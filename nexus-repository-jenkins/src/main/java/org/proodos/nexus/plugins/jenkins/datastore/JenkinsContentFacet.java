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
package org.proodos.nexus.plugins.jenkins.datastore;

import java.io.IOException;
import java.util.Optional;

import org.sonatype.nexus.repository.content.facet.ContentFacet;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Payload;

public interface JenkinsContentFacet
    extends ContentFacet
{
  Optional<Content> get(String path) throws IOException;

  Content putArchive(String path, String filename, String version, Payload payload);

  Content putIndex(String path, Payload payload);
}
