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
/*global Ext, NX*/

/**
 * Repository "Settings" form for a Apk Proxy repository
 */
Ext.define('NX.jenkins.view.repository.recipe.JenkinsProxy', {
  extend: 'NX.coreui.view.repository.RepositorySettingsForm',
  alias: 'widget.nx-coreui-repository-jenkins-proxy',
  requires: [
    'NX.coreui.view.repository.facet.ProxyFacet',
    'NX.coreui.view.repository.facet.StorageFacet',
    'NX.coreui.view.repository.facet.HttpClientFacet',
    'NX.coreui.view.repository.facet.NegativeCacheFacet'
  ],

  /**
   * @override
   */
  initComponent: function () {
    var me = this;

    me.items = [
      {xtype: 'nx-coreui-repository-proxy-facet'},
      {xtype: 'nx-coreui-repository-storage-facet'},
      {xtype: 'nx-coreui-repository-negativecache-facet'},
      {xtype: 'nx-coreui-repository-httpclient-facet'}
    ];

    me.callParent();
  }
});
