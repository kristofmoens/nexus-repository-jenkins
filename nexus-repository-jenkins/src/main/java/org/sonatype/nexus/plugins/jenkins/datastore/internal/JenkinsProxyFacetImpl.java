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
package org.sonatype.nexus.plugins.jenkins.datastore.internal;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Named;

import org.sonatype.nexus.plugins.jenkins.datastore.JenkinsContentFacet;
import org.sonatype.nexus.plugins.jenkins.internal.AssetKind;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.content.facet.ContentProxyFacetSupport;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;

@Named
public class JenkinsProxyFacetImpl
    extends ContentProxyFacetSupport
{
  @Nullable
  @Override
  protected Content getCachedContent(final Context context) throws IOException {
    AssetKind assetKind = context.getAttributes().require(AssetKind.class);
    TokenMatcher.State matcherState = JenkinsPathUtils.matcherState(context);
    switch (assetKind) {
      case ARCHIVE:
        return content().get(JenkinsPathUtils.archivePath(JenkinsPathUtils.path(matcherState), JenkinsPathUtils.name(matcherState),
            JenkinsPathUtils.version(matcherState))).orElse(null);
      case JENKINS_INDEX:
        return content().get(JenkinsPathUtils.path(matcherState)).orElse(null);
      default:
        throw new IllegalStateException();
    }
  }

  @Override
  protected Content store(final Context context, final Content content) throws IOException {
    AssetKind assetKind = context.getAttributes().require(AssetKind.class);
    TokenMatcher.State matcherState = JenkinsPathUtils.matcherState(context);
    switch (assetKind) {
      case ARCHIVE:
        log.debug("ARCHIVE" + JenkinsPathUtils.path(matcherState));
        return putArchive(JenkinsPathUtils.path(matcherState), JenkinsPathUtils.name(matcherState),
            JenkinsPathUtils.version(matcherState), content);
      case JENKINS_INDEX:
        log.debug(("APK_INDEX" + JenkinsPathUtils.path(matcherState)));
        return putIndex(JenkinsPathUtils.path(matcherState), content);
      default:
        throw new IllegalStateException();
    }
  }

  private Content putArchive(
      final String path,
      final String filename,
      final String version,
      final Content content) throws IOException
  {
    return content().putArchive(path, filename, version, content);
  }

  private Content putIndex(final String path, final Content content) throws IOException {
    return content().putIndex(path, content);
  }

  @Override
  protected void doValidate(final Configuration configuration) throws Exception {
    super.doValidate(configuration);
  }

  @Override
  protected String getUrl(@Nonnull final Context context) {
    return context.getRequest().getPath().substring(1);
  }

  private JenkinsContentFacet content() {
    return getRepository().facet(JenkinsContentFacet.class);
  }
}
