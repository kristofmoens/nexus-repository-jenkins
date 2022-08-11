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

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;

import static com.google.common.base.Preconditions.checkNotNull;

public final class JenkinsPathUtils
{

  /**
   * * Returns the name from a {@link TokenMatcher.State}.
   */
  public static String path(final TokenMatcher.State state) {
    return match(state, "path");
  }

  public static String buildIndexPath() {
    return "/update-center.json";
  }

  public static String version(final TokenMatcher.State state) {
    String filename = match(state, "path");
    int lastPathIdx = filename.lastIndexOf('/',filename.length() - 2);
    int beforeLastIdx = filename.lastIndexOf('/',lastPathIdx - 1);
    return filename.substring(beforeLastIdx,lastPathIdx);
  }

  /**
   * Utility method encapsulating getting a particular token by name from a matcher, including preconditions.
   */
  private static String match(final TokenMatcher.State state, final String name) {
    checkNotNull(state);
    String result = state.getTokens().get(name);
    checkNotNull(result);
    return result;
  }

  /**
   * Builds a path to an archive for a particular path and filename.
   */
  public static String path(final String path, final String filename) {
    return path + "/" + filename;
  }

  public static String name(final TokenMatcher.State state) {
    return match(state, "filename");
  }

  /**
   * Returns the filename from a {@link TokenMatcher.State}.
   */
  public static String filename(final TokenMatcher.State state) {
    return match(state, "filename");
  }

  /**
   * Builds a path to the archive for a particular path
   */
  public static String archivePath(final String path, final String filename, final String version) {
    return '/' + path + "/" + version + '/' + filename + ".hpi";
  }
  /**
   * Returns the {@link TokenMatcher.State} for the content.
   */
  public static TokenMatcher.State matcherState(final Context context) {
    return context.getAttributes().require(TokenMatcher.State.class);
  }

  private JenkinsPathUtils() {
    // empty
  }
}
