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
Ext.define('NX.jenkins.util.JenkinsRepositoryUrls', {
  '@aggregate_priority': 90,

  singleton: true,
  requires: [
    'NX.coreui.util.RepositoryUrls',
    'NX.util.Url'
  ]
}, function(self) {
  NX.coreui.util.RepositoryUrls.addRepositoryUrlStrategy('jenkins', function(me, assetModel) {
    var repositoryName = assetModel.get('repositoryName'), assetName = assetModel.get('name');
    return NX.util.Url.asLink(
        NX.util.Url.baseUrl + '/repository/' + encodeURIComponent(repositoryName) + '/' + encodeURI(assetName),
        assetName);
  });
});
