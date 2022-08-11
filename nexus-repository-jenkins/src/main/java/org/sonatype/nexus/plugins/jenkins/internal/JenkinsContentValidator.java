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

import org.sonatype.goodies.common.ComponentSupport;
import org.sonatype.nexus.common.io.InputStreamSupplier;
import org.sonatype.nexus.mime.MimeRulesSource;
import org.sonatype.nexus.repository.mime.ContentValidator;
import org.sonatype.nexus.repository.mime.DefaultContentValidator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

@Named(JenkinsFormat.NAME)
@Singleton
public class JenkinsContentValidator
    extends ComponentSupport
    implements ContentValidator
{
  private final DefaultContentValidator defaultContentValidator;

  @Inject
  public JenkinsContentValidator(final DefaultContentValidator defaultContentValidator) {
    this.defaultContentValidator = checkNotNull(defaultContentValidator);
  }

  @Nonnull
  @Override
  public String determineContentType(final boolean strictContentTypeValidation,
                                     final InputStreamSupplier contentSupplier,
                                     @Nullable final MimeRulesSource mimeRulesSource,
                                     @Nullable final String contentName,
                                     @Nullable final String declaredContentType) throws IOException
  {
    if (contentName != null) {
      if (contentName.endsWith(".hpi")) {
        return defaultContentValidator.determineContentType(
            false,
            contentSupplier,
            mimeRulesSource,
            contentName,
            "application/x-gzip");
      }
    }
    return defaultContentValidator.determineContentType(
        strictContentTypeValidation,
        contentSupplier,
        mimeRulesSource,
        contentName,
        declaredContentType
    );
  }
}