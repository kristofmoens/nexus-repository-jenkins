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
package org.proodos.nexus.plugins.jenkins.orient.internal;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility Methods for working APK routes and paths
 */
@Named
@Singleton
public class JenkinsPathUtils
{

  /**
   * * Returns the name from a {@link TokenMatcher.State}.
   */
  public String path(final TokenMatcher.State state) {
    return match(state, "path");
  }

  public String buildIndexPath() {
    return "/update-center.json";
  }

  public String version(final TokenMatcher.State state) {
    return match(state, "version");
  }

  /**
   * Utility method encapsulating getting a particular token by name from a matcher, including preconditions.
   */
  private String match(TokenMatcher.State state, String name) {
    checkNotNull(state);
    String result = state.getTokens().get(name);
    checkNotNull(result);
    return result;
  }

  public JenkinsPathUtils() {
    // empty
  }

  /**
   * Builds a path to an archive for a particular path and filename.
   */
  public String path(final String path, final String filename) {
    return path + "/" + filename;
  }

  public String name(final TokenMatcher.State state) {
    return match(state, "filename");
  }

  /**
   * Returns the filename from a {@link TokenMatcher.State}.
   */
  public String filename(final TokenMatcher.State state) {
    return match(state, "filename");
  }

  /**
   * Builds a path to the archive for a particular path
   */
  public String archivePath(final String path, final String filename, final String version) {
    return path + "/" + version + "/" + filename;
  }
  /**
   * Returns the {@link TokenMatcher.State} for the content.
   */
  public TokenMatcher.State matcherState(final Context context) {
    return context.getAttributes().require(TokenMatcher.State.class);
  }
}
