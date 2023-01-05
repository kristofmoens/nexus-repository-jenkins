/*
 * Proodos BV Open Source Version
 * Copyright (c) 2022-present Proodos BV
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License
 * Version 1.0, which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * All trademarks are the property of their respective owners.
 */
package org.proodos.nexus.plugins.jenkins.internal.fixtures

import javax.annotation.Nonnull
import org.sonatype.nexus.repository.Repository
import org.sonatype.nexus.repository.config.Configuration
import org.sonatype.nexus.testsuite.testsupport.fixtures.ConfigurationRecipes
import groovy.transform.CompileStatic

/**
 * Factory for apk {@link Repository} {@link Configuration}
 */
@CompileStatic
trait JenkinsRepoRecipes
    extends ConfigurationRecipes
{
  @Nonnull
  Repository createApkProxy(final String name, final String remoteUrl)
  {
    createRepository(createProxy(name, 'jenkins-proxy', remoteUrl))
  }

  abstract Repository createRepository(final Configuration configuration)
}
