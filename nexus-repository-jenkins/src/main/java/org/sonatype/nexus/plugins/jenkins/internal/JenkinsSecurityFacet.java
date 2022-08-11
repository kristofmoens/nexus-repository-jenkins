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
package org.sonatype.nexus.plugins.jenkins.internal;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.repository.security.ContentPermissionChecker;
import org.sonatype.nexus.repository.security.SecurityFacetSupport;
import org.sonatype.nexus.repository.security.VariableResolverAdapter;

/**
 * Jenkins format security facet.
 */
@Named
public class JenkinsSecurityFacet
    extends SecurityFacetSupport
{
  @Inject
  public JenkinsSecurityFacet(final JenkinsFormatSecurityContributor securityResource,
                              @Named("simple") final VariableResolverAdapter variableResolverAdapter,
                              final ContentPermissionChecker contentPermissionChecker)
  {
    super(securityResource, variableResolverAdapter, contentPermissionChecker);
  }
}
