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
package org.proodos.nexus.plugins.jenkins.datastore.internal.store;

import javax.inject.Named;

import org.proodos.nexus.plugins.jenkins.internal.JenkinsFormat;
import org.sonatype.nexus.repository.content.store.FormatStoreModule;

/**
 * Configures the content store bindings for a apk format.
 */
@Named(JenkinsFormat.NAME)
public class JenkinsStoreModule
    extends FormatStoreModule<JenkinsContentRepositoryDAO,
        JenkinsComponentDAO,
        JenkinsAssetDAO,
        JenkinsAssetBlobDAO>
{
  // nothing to add...
}
