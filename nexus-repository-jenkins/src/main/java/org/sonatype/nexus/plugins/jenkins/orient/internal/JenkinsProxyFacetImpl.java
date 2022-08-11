/*
 * Proodos BV Open Source Version
 * Copyright (c) 2022-present Proodos BV
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * All trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.jenkins.orient.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.plugins.jenkins.datastore.internal.JenkinsContentFacetImpl;
import org.sonatype.nexus.plugins.jenkins.internal.AssetKind;
import org.sonatype.nexus.repository.cache.CacheInfo;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.proxy.ProxyFacetSupport;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.Bucket;
import org.sonatype.nexus.repository.storage.Component;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.StorageTx;
import org.sonatype.nexus.repository.transaction.TransactionalStoreBlob;
import org.sonatype.nexus.repository.transaction.TransactionalTouchBlob;
import org.sonatype.nexus.repository.transaction.TransactionalTouchMetadata;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;
import org.sonatype.nexus.repository.view.payloads.TempBlob;
import org.sonatype.nexus.transaction.UnitOfWork;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.plugins.jenkins.internal.AssetKind.JENKINS_INDEX;
import static org.sonatype.nexus.plugins.jenkins.internal.AssetKind.ARCHIVE;
import static org.sonatype.nexus.repository.storage.AssetEntityAdapter.P_ASSET_KIND;
import static org.sonatype.nexus.repository.storage.AssetManager.DEFAULT_LAST_DOWNLOADED_INTERVAL;

@Named
public class JenkinsProxyFacetImpl
    extends ProxyFacetSupport
{
  private final JenkinsPathUtils pathUtils;
  private final JenkinsDataAccess dataAccess;

  @Inject
  public JenkinsProxyFacetImpl(final JenkinsPathUtils pathUtils,
                               final JenkinsDataAccess dataAccess)
  {
    this.pathUtils = checkNotNull(pathUtils);
    this.dataAccess = checkNotNull(dataAccess);
  }

  @Nullable
  @Override
  protected Content getCachedContent(final Context context) throws IOException {
    AssetKind assetKind = context.getAttributes().require(AssetKind.class);
    TokenMatcher.State matcherState = pathUtils.matcherState(context);
    switch (assetKind) {
      case ARCHIVE:
        return getAsset(pathUtils.archivePath(pathUtils.path(matcherState), pathUtils.name(matcherState), pathUtils.version(matcherState)));
      case JENKINS_INDEX:
        return getAsset(pathUtils.buildIndexPath());
      default:
        throw new IllegalStateException();
    }
  }

  @TransactionalTouchBlob
  protected Content getAsset(final String name) {
    StorageTx tx = UnitOfWork.currentTx();
    Asset asset = dataAccess.findAsset(tx, tx.findBucket(getRepository()), name);
    if (asset == null) {
      return null;
    }
    if (asset.markAsDownloaded(DEFAULT_LAST_DOWNLOADED_INTERVAL)) {
      tx.saveAsset(asset);
    }
    return dataAccess.toContent(asset, tx.requireBlob(asset.requireBlobRef()));
  }

  @Override
  protected Content store(final Context context, final Content content) throws IOException {
    AssetKind assetKind = context.getAttributes().require(AssetKind.class);
    TokenMatcher.State matcherState = pathUtils.matcherState(context);
    switch (assetKind) {
      case ARCHIVE:
        log.debug("ARCHIVE" + pathUtils.path(matcherState));
        return putArchive(pathUtils.path(matcherState), pathUtils.name(matcherState), pathUtils.version(matcherState), content);
      case JENKINS_INDEX:
        // modify the jenkins index, so that it will point to the current nexus repo
        String repoName = context.getRepository().getName();
        String host = context.getRequest().getHeaders().get("Host");
        String nexusBaseRepoUrl = "https://" + host + "/" + repoName;
        log.debug("Store JENKINS_INDEX for repo {}", nexusBaseRepoUrl);
        return putIndex(nexusBaseRepoUrl, content);
      default:
        throw new IllegalStateException();
    }
  }

  private Content putArchive(final String path, final String filename, final String version, final Content content) throws IOException {
    StorageFacet storageFacet = facet(StorageFacet.class);
    try (TempBlob tempBlob = storageFacet.createTempBlob(content.openInputStream(), JenkinsDataAccess.HASH_ALGORITHMS)) {
      return doPutArchive(path, filename, version, tempBlob, content);
    }
  }

  @TransactionalStoreBlob
  protected Content doPutArchive(final String path,
                                 final String name,
                                 final String version,
                                 final TempBlob archiveContent,
                                 final Payload payload) throws IOException
  {
    StorageTx tx = UnitOfWork.currentTx();
    Bucket bucket = tx.findBucket(getRepository());
    String assetPath = pathUtils.archivePath(path, name, version);

    Component component = dataAccess.findComponent(tx, getRepository(), name, version);
    if (component == null) {
      component = tx.createComponent(bucket, getRepository().getFormat())
          .name(name)
          .version(version);
    }
    tx.saveComponent(component);

    Asset asset = dataAccess.findAsset(tx, bucket, assetPath);
    if (asset == null) {
      asset = tx.createAsset(bucket, component);
      asset.name(assetPath);
      asset.formatAttributes().set(P_ASSET_KIND, ARCHIVE.name());
    }
    return dataAccess.saveAsset(tx, asset, archiveContent, payload);
  }

  private Content putIndex(final String repoBaseUrl, final Content content) throws IOException {
    StorageFacet storageFacet = facet(StorageFacet.class);
    ByteArrayOutputStream baos = JenkinsContentFacetImpl.updateUrlReferenceForUpdateCenterJson(content.getPayload(),repoBaseUrl);
    byte[] buffer = baos.toByteArray();
    try (TempBlob tempBlob = storageFacet.createTempBlob(new ByteArrayInputStream(buffer), JenkinsDataAccess.HASH_ALGORITHMS)) {
      return doPutIndex(repoBaseUrl, tempBlob, content);
    }
  }

  @TransactionalStoreBlob
  protected Content doPutIndex(final String baseUrl,
                               final TempBlob metadataContent,
                               final Payload payload) throws IOException
  {
    StorageTx tx = UnitOfWork.currentTx();
    Bucket bucket = tx.findBucket(getRepository());

    String assetPath = pathUtils.buildIndexPath();

    Asset asset = dataAccess.findAsset(tx, bucket, assetPath);
    if (asset == null) {
      asset = tx.createAsset(bucket, getRepository().getFormat());
      asset.name(assetPath);
      asset.formatAttributes().set(P_ASSET_KIND, JENKINS_INDEX.name());
    }
    return dataAccess.saveAsset(tx, asset, metadataContent, payload);
  }

  @Override
  protected void doValidate(final Configuration configuration) throws Exception {
    super.doValidate(configuration);
  }

  @Override
  protected String getUrl(@Nonnull final Context context) {
    return context.getRequest().getPath().substring(1);
  }

  @Override
  protected void indicateVerified(final Context context, final Content content, final CacheInfo cacheInfo)
      throws IOException
  {
    setCacheInfo(content, cacheInfo);
  }

  @TransactionalTouchMetadata
  public void setCacheInfo(final Content content, final CacheInfo cacheInfo) throws IOException {
    StorageTx tx = UnitOfWork.currentTx();
    Asset asset = Content.findAsset(tx, tx.findBucket(getRepository()), content);
    if (asset == null) {
      log.debug(
          "Attempting to set cache info for non-existent APK asset {}", content.getAttributes().require(Asset.class)
      );
      return;
    }
    log.debug("Updating cacheInfo of {} to {}", asset, cacheInfo);
    CacheInfo.applyToAsset(asset, cacheInfo);
    tx.saveAsset(asset);
  }
}
