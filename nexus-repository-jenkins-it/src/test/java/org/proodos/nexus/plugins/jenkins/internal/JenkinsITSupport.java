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
package org.proodos.nexus.plugins.jenkins.internal;

import java.net.URL;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.proodos.nexus.plugins.jenkins.internal.fixtures.RepositoryRuleJenkins;
import org.sonatype.nexus.pax.exam.NexusPaxExamSupport;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.testsuite.helpers.ComponentAssetTestHelper;
import org.sonatype.nexus.testsuite.testsupport.RepositoryITSupport;

import org.junit.Rule;

import static com.google.common.base.Preconditions.checkNotNull;

public class JenkinsITSupport
    extends RepositoryITSupport
{
  @Rule
  public RepositoryRuleJenkins repos = new RepositoryRuleJenkins(() -> repositoryManager);

  @Inject
  protected ComponentAssetTestHelper componentAssetTestHelper;

  @Override
  protected RepositoryRuleJenkins createRepositoryRule() {
    return new RepositoryRuleJenkins(() -> repositoryManager);
  }

  public JenkinsITSupport() {
    testData.addDirectory(NexusPaxExamSupport.resolveBaseFile("target/it-resources/jenkins"));
  }

  @Nonnull
  protected JenkinsClient jenkinsClient(final Repository repository) throws Exception {
    checkNotNull(repository);
    return jenkinsClient(repositoryBaseUrl(repository));
  }

  protected JenkinsClient jenkinsClient(final URL repositoryUrl) throws Exception {
    return new JenkinsClient(
        clientBuilder(repositoryUrl).build(),
        clientContext(),
        repositoryUrl.toURI()
    );
  }
}
