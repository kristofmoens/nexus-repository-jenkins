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

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.sonatype.goodies.httpfixture.server.fluent.Behaviours;
import org.sonatype.goodies.httpfixture.server.fluent.Server;
import org.sonatype.nexus.pax.exam.NexusPaxExamSupport;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.http.HttpStatus;
import org.sonatype.nexus.testsuite.testsupport.NexusITSupport;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.sonatype.nexus.testsuite.testsupport.FormatClientSupport.status;

public class JenkinsProxyIT
    extends JenkinsITSupport
{
  private static final String UPDATE_CENTER_INDEX = "update-center.json";

  private static final String PLUGINS_DIRECTORY = "plugins";
  public static final String JENKINS_PLUGIN_EXTENSION = ".hpi";

  private static final String CRUNCH_SECURITY_PLUGINNAME = "42crunch-security-audit";
  private static final String CRUNCH_SECURITY_VERSION = "5.1";

  private static final String ANCHORCHAIN_PLUGINNAME = "AnchorChain";
  private static final String ANCHORCHAIN_VERSION = "1.0";

  private static final String PATH_UPDATE_CENTER = UPDATE_CENTER_INDEX;

  private static final String PATH_CRUNCH_SECURITY = toDir(PLUGINS_DIRECTORY, CRUNCH_SECURITY_PLUGINNAME, CRUNCH_SECURITY_VERSION, CRUNCH_SECURITY_PLUGINNAME + JENKINS_PLUGIN_EXTENSION);
  private static final String PATH_ANCHORCHAIN = toDir(PLUGINS_DIRECTORY, ANCHORCHAIN_PLUGINNAME, ANCHORCHAIN_VERSION, ANCHORCHAIN_PLUGINNAME + JENKINS_PLUGIN_EXTENSION);
  public static final String REFERENCE_UPDATE_CENTER_JSON = "wantedUpdateCenter.json";

  private JenkinsClient proxyClient;

  private Repository proxyRepo;

  private Server server;

  private static String toDir(String... dirs) {
    StringBuilder sb = new StringBuilder(dirs.length*10);
    sb.append(dirs[0]);
    for (int i = 1; i < dirs.length; i++) {
      sb.append("/").append(dirs[i]);
    }
    return sb.toString();
  }

  @Configuration
  public static Option[] configureNexus() {
    return NexusPaxExamSupport.options(
        NexusITSupport.configureNexusBase(),
        nexusFeature("org.sonatype.nexus.plugins", "nexus-repository-jenkins")
    );
  }

  @Before
  public void setup() throws Exception {
    server = Server.withPort(0)
        .serve("/" + PATH_UPDATE_CENTER)
        .withBehaviours(Behaviours.file(testData.resolveFile(UPDATE_CENTER_INDEX)))
        .serve("/" + PATH_CRUNCH_SECURITY)
        .withBehaviours(Behaviours.file(testData.resolveFile(CRUNCH_SECURITY_PLUGINNAME + JENKINS_PLUGIN_EXTENSION)))
        .serve("/" + PATH_ANCHORCHAIN)
        .withBehaviours(Behaviours.file(testData.resolveFile(ANCHORCHAIN_PLUGINNAME + JENKINS_PLUGIN_EXTENSION)))
        .start();

    proxyRepo = repos.createApkProxy("jenkins-test-proxy", server.getUrl().toExternalForm());
    proxyClient = jenkinsClient(proxyRepo);
  }

  @Test
  public void nonExistingRemoteUrlProduces404() throws Exception {
    proxyClient.get("stupid/path");
    assertThat(status(proxyClient.get("stupid/path")), is(HttpStatus.NOT_FOUND));
  }

  @Test
  public void retrieveJenkinsUpdateCenterFromProxyWhenRemoteOnline() throws Exception {
    CloseableHttpResponse response = proxyClient.get(PATH_UPDATE_CENTER);
    assertThat(status(response), is(HttpStatus.OK));

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(10000);
    IOUtils.copy(response.getEntity().getContent(), byteArrayOutputStream);
    byte[] wantedContent = Files.readAllBytes(testData.resolveFile(REFERENCE_UPDATE_CENTER_JSON).toPath());
    String expected = new String(wantedContent).replace("{{ target url }}",nexusUrl.toString().replace("http","https") + "jenkins-test-proxy");
    assertEquals("Update-center.json url content should have been changed correctly", expected, byteArrayOutputStream.toString());

    // assertTrue(componentAssetTestHelper.assetExists(proxyRepo, PATH_UPDATE_CENTER));
  }

  @Test
  public void retrieveApkArchiveFromProxyWhenRemoteOnline() throws Exception {
    assertThat(status(proxyClient.get(PATH_CRUNCH_SECURITY)), is(HttpStatus.OK));
    assertThat(status(proxyClient.get(PATH_ANCHORCHAIN)), is(HttpStatus.OK));

    assertTrue(componentAssetTestHelper.assetExists(proxyRepo, PATH_CRUNCH_SECURITY));
    assertTrue(componentAssetTestHelper.assetExists(proxyRepo, PATH_ANCHORCHAIN));
    assertTrue(componentAssetTestHelper.componentExists(proxyRepo, CRUNCH_SECURITY_PLUGINNAME, CRUNCH_SECURITY_VERSION));
    assertTrue(componentAssetTestHelper.componentExists(proxyRepo, ANCHORCHAIN_PLUGINNAME, ANCHORCHAIN_VERSION));
  }

  @After
  public void tearDown() throws Exception {
    server.stop();
  }
}
