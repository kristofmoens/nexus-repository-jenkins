package org.proodos.nexus.plugins.jenkins.datastore.internal;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.proodos.nexus.plugins.jenkins.internal.AssetKind;
import org.sonatype.nexus.common.collect.AttributesMap;
import org.sonatype.nexus.repository.http.HttpMethods;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Matcher;
import org.sonatype.nexus.repository.view.Request;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;

import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class JenkinsRecipeSupportTest {

    @Mock
    Context context;
    @Mock
    Request requestClass;

    @Test
    public void testJenkinsArchiveMatcher() {
        Matcher matcher = JenkinsRecipeSupport.archiveMatcher();
        Map<String, String> matches = checkMatcherForUrl(AssetKind.ARCHIVE,HttpMethods.GET, "/plugins/worktile/1.1.17/worktile.hpi", matcher);
        Assert.assertEquals("plugins/worktile",matches.get("path"));
        Assert.assertEquals("1.1.17",matches.get("version"));
        Assert.assertEquals("worktile.hpi",matches.get("filename"));
        matches = checkMatcherForUrl(AssetKind.ARCHIVE, HttpMethods.GET, "/plugins/worktile/1.1.17/worktile.hpi?extra-args=true", matcher);
        Assert.assertEquals("plugins/worktile",matches.get("path"));
        Assert.assertEquals("1.1.17",matches.get("version"));
        Assert.assertEquals("worktile.hpi",matches.get("filename"));
        matches = checkMatcherForUrl(AssetKind.ARCHIVE, HttpMethods.GET, "/plugins/worktile/1.1.17/worktile.exe?extra-args=true", matcher);
        Assert.assertNull(matches);
        matches = checkMatcherForUrl(AssetKind.ARCHIVE, HttpMethods.GET, "/war/2.362/jenkins.war", matcher);
        Assert.assertEquals("war",matches.get("path"));
        Assert.assertEquals("2.362",matches.get("version"));
        Assert.assertEquals("jenkins.war",matches.get("filename"));
        matches = checkMatcherForUrl(AssetKind.ARCHIVE, HttpMethods.GET, "/war/2.362/jenkins.war?latest", matcher);
        Assert.assertEquals("war",matches.get("path"));
        Assert.assertEquals("2.362",matches.get("version"));
        Assert.assertEquals("jenkins.war",matches.get("filename"));
    }

    @Test
    public void testJenkinsUpdateCenterMatcher() {
        Matcher matcher = JenkinsRecipeSupport.jenkinsUpdateCenterIndexMatcher();
        Map<String, String> matches = checkMatcherForUrl(AssetKind.JENKINS_INDEX, HttpMethods.GET, "/update-invalid.json", matcher);
        assertNull(matches);
        matches = checkMatcherForUrl(AssetKind.JENKINS_INDEX, HttpMethods.GET, "/update-center.json", matcher);
        assertNotNull(matches);
        matches = checkMatcherForUrl(AssetKind.JENKINS_INDEX, HttpMethods.GET, "/update-center.json?test", matcher);
        assertNotNull(matches);
        matches = checkMatcherForUrl(AssetKind.JENKINS_INDEX, HttpMethods.GET, "/another-update-center.json", matcher);
        assertNull(matches);
        matches = checkMatcherForUrl(AssetKind.JENKINS_INDEX, HttpMethods.GET, "/another-update-center.json?argum=false", matcher);
        assertNull(matches);
    }

    private Map<String, String> checkMatcherForUrl(AssetKind assertKind, String action, String url, Matcher matcher) {
        AttributesMap attrMap = new AttributesMap();
        Mockito.when(context.getAttributes()).thenReturn(attrMap);
        Mockito.when(requestClass.getAction()).thenReturn(action);
        Mockito.when(requestClass.getPath()).thenReturn(url);
        Mockito.when(context.getRequest()).thenReturn(requestClass);
        if(matcher.matches(context)) {
            Assert.assertEquals(attrMap.get(AssetKind.class),assertKind);
            return attrMap.get(TokenMatcher.State.class).getTokens();
        } else {
            return null;
        }
    }

    public void testArchiveMatcher() {
    }

    public void testBuildTokenMatcherForPatternAndAssetKind() {
    }
}