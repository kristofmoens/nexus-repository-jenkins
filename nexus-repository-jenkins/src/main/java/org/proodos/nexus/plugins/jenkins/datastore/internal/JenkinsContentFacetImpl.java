/*
 * Proodos BV Open Source Version
 * Copyright (c) 2018-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * All trademarks are the property of their respective owners.
 */
package org.proodos.nexus.plugins.jenkins.datastore.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.common.hash.HashAlgorithm;
import org.proodos.nexus.plugins.jenkins.datastore.JenkinsContentFacet;
import org.proodos.nexus.plugins.jenkins.internal.AssetKind;
import org.proodos.nexus.plugins.jenkins.updatecenter.internal.UpdateCenterModifier;
import org.sonatype.nexus.repository.content.facet.ContentFacetSupport;
import org.sonatype.nexus.repository.content.fluent.FluentAsset;
import org.sonatype.nexus.repository.content.fluent.FluentComponent;
import org.sonatype.nexus.repository.content.store.FormatStoreManager;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.payloads.BytesPayload;
import org.sonatype.nexus.repository.view.payloads.TempBlob;

import com.google.common.collect.ImmutableList;

import static org.sonatype.nexus.common.hash.HashAlgorithm.SHA1;

@Named
public class JenkinsContentFacetImpl
    extends ContentFacetSupport
    implements JenkinsContentFacet
{
  public static final List<HashAlgorithm> HASH_ALGORITHMS = ImmutableList.of(SHA1);
  public static final int INITIAL_UPDATECENTER_BUFFER_SIZE = 100000;

  @Inject
  public JenkinsContentFacetImpl(final FormatStoreManager formatStoreManager) {
    super(formatStoreManager);
  }

  @Override
  public Optional<Content> get(final String path) throws IOException {
    return assets()
        .path(path)
        .find()
        .map(FluentAsset::download);
  }

  @Override
  public Content putArchive(final String path, final String filename, final String version, final Payload payload) {
    String assetPath = JenkinsPathUtils.archivePath(path, filename, version);
    try (TempBlob tempBlob = blobs().ingest(payload, HASH_ALGORITHMS)) {
      FluentComponent component = components()
          .name(filename)
          .version(version)
          .getOrCreate();

      return assets().path(assetPath)
          .component(component)
          .kind(AssetKind.ARCHIVE.name())
          .blob(tempBlob)
          .save()
          .markAsCached(payload)
          .download();
    }
  }

  @Override
  public Content putIndex(final String path, final Payload payload) {
    String assetPath = JenkinsPathUtils.buildIndexPath();

    String baseUrlPath = "nexus";
    ByteArrayOutputStream baos = updateUrlReferenceForUpdateCenterJson(payload, baseUrlPath);
    BytesPayload newPayload = new BytesPayload(baos.toByteArray(), payload.getContentType());
    try (TempBlob tempBlob = blobs().ingest(newPayload, HASH_ALGORITHMS)) {
      return assets().path(assetPath)
          .kind(AssetKind.JENKINS_INDEX.name())
          .blob(tempBlob)
          .save()
          .markAsCached(payload)
          .download();
    }
  }

  public static ByteArrayOutputStream updateUrlReferenceForUpdateCenterJson(Payload payload, String baseUrlPath) {
    UpdateCenterModifier modifier = new UpdateCenterModifier(baseUrlPath);
    int payloadSize = (int) payload.getSize();
    ByteArrayOutputStream baos = payloadSize > 100 ? new ByteArrayOutputStream(payloadSize) : new ByteArrayOutputStream(INITIAL_UPDATECENTER_BUFFER_SIZE);
    try {
      modifier.modifyUpdateCenterJson(payload.openInputStream(), baos);
    } catch (IOException e) {
      throw new RuntimeException("Failed to process the json", e);
    }
    return baos;
  }

}
