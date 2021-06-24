package org.obiba.oidc.shiro.realm;

import org.assertj.core.util.Lists;
import org.assertj.core.util.Maps;
import org.junit.Assert;
import org.junit.Test;
import org.obiba.oidc.OIDCConfiguration;

import java.util.Map;
import java.util.Set;

public class OIDCGroupsExtractorTest {

  @Test
  public void testGroupsDefaultClaim() {
    OIDCGroupsExtractor extractor = new DefaultOIDCGroupsExtractor();
    OIDCConfiguration configuration = new OIDCConfiguration();
    verifyGroups(extractor.extractGroups(configuration, makeUserInfo("groups")));
  }

  @Test
  public void testGroupsClaim() {
    OIDCGroupsExtractor extractor = new DefaultOIDCGroupsExtractor();
    OIDCConfiguration configuration = new OIDCConfiguration();
    configuration.setCustomParam(OIDCRealm.GROUPS_CLAIM_PARAM, "xyz");
    verifyGroups(extractor.extractGroups(configuration, makeUserInfo("xyz")));
  }

  @Test
  public void testGroupsStringClaim() {
    OIDCGroupsExtractor extractor = new DefaultOIDCGroupsExtractor();
    OIDCConfiguration configuration = new OIDCConfiguration();
    configuration.setCustomParam(OIDCRealm.GROUPS_CLAIM_PARAM, "xyz");
    verifyGroups(extractor.extractGroups(configuration, makeUserInfoGroupsString("xyz")));
  }

  @Test
  public void testGroupsClaimJS() {
    OIDCGroupsExtractor extractor = new DefaultOIDCGroupsExtractor();
    OIDCConfiguration configuration = new OIDCConfiguration();
    configuration.setCustomParam(OIDCRealm.GROUPS_CLAIM_PARAM, "groups");
    configuration.setCustomParam(OIDCRealm.GROUPS_JS_PARAM, "userInfo.xyz");
    verifyGroups(extractor.extractGroups(configuration, makeUserInfo("xyz")));
  }

  @Test
  public void testGroupsClaimStringJS() {
    OIDCGroupsExtractor extractor = new DefaultOIDCGroupsExtractor();
    OIDCConfiguration configuration = new OIDCConfiguration();
    configuration.setCustomParam(OIDCRealm.GROUPS_CLAIM_PARAM, "groups");
    configuration.setCustomParam(OIDCRealm.GROUPS_JS_PARAM, "userInfo.xyz.split(' ')");
    verifyGroups(extractor.extractGroups(configuration, makeUserInfoGroupsString("xyz")));
  }

  @Test
  public void testGroupsClaimJSArray() {
    OIDCGroupsExtractor extractor = new DefaultOIDCGroupsExtractor();
    OIDCConfiguration configuration = new OIDCConfiguration();
    configuration.setCustomParam(OIDCRealm.GROUPS_CLAIM_PARAM, "groups");
    configuration.setCustomParam(OIDCRealm.GROUPS_JS_PARAM, "['a', 'b', 'c']");
    verifyGroups(extractor.extractGroups(configuration, makeUserInfo("xyz")));
  }

  @Test
  public void testGroupsClaimJS2Single() {
    OIDCGroupsExtractor extractor = new DefaultOIDCGroupsExtractor();
    OIDCConfiguration configuration = new OIDCConfiguration();
    configuration.setCustomParam(OIDCRealm.GROUPS_CLAIM_PARAM, "groups");
    configuration.setCustomParam(OIDCRealm.GROUPS_JS_PARAM, "'a'");
    Set<String> groups = extractor.extractGroups(configuration, makeUserInfo("xyz"));
    Assert.assertEquals(1, groups.size());
    Assert.assertEquals("a", groups.iterator().next());
  }

  @Test
  public void testGroupsClaimJSNull() {
    OIDCGroupsExtractor extractor = new DefaultOIDCGroupsExtractor();
    OIDCConfiguration configuration = new OIDCConfiguration();
    configuration.setCustomParam(OIDCRealm.GROUPS_CLAIM_PARAM, "groups");
    configuration.setCustomParam(OIDCRealm.GROUPS_JS_PARAM, "null");
    Assert.assertEquals(0, extractor.extractGroups(configuration, makeUserInfo("xyz")).size());
  }

  @Test
  public void testGroupsClaimJSUndefined() {
    OIDCGroupsExtractor extractor = new DefaultOIDCGroupsExtractor();
    OIDCConfiguration configuration = new OIDCConfiguration();
    configuration.setCustomParam(OIDCRealm.GROUPS_CLAIM_PARAM, "groups");
    configuration.setCustomParam(OIDCRealm.GROUPS_JS_PARAM, "undefined");
    Assert.assertEquals(0, extractor.extractGroups(configuration, makeUserInfo("xyz")).size());
  }

  private Map<String, Object> makeUserInfo(String key) {
    Map<String, Object> userInfo = Maps.newHashMap();
    userInfo.put(key, Lists.newArrayList("a", "b", "c"));
    return userInfo;
  }

  private Map<String, Object> makeUserInfoGroupsString(String key) {
    Map<String, Object> userInfo = Maps.newHashMap();
    userInfo.put(key, "a b c");
    return userInfo;
  }

  private void verifyGroups(Set<String> groups) {
    Assert.assertEquals(3, groups.size());
    Assert.assertTrue(groups.contains("a"));
    Assert.assertTrue(groups.contains("b"));
    Assert.assertTrue(groups.contains("c"));
  }
}
