package org.obiba.oidc.shiro.realm;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.nimbusds.jwt.JWTClaimsSet;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.obiba.oidc.OIDCConfiguration;
import org.obiba.oidc.OIDCCredentials;
import org.obiba.oidc.shiro.authc.OIDCAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Realm based on OpenID connect token, after all authorization and identification stuff has happen.
 */
public class OIDCRealm extends AuthorizingRealm {

  private static final Logger log = LoggerFactory.getLogger(OIDCRealm.class);

  /**
   * Group names to apply systematically.
   */
  public static final String GROUPS_PARAM = "groups";

  /**
   * JS function to apply to retrieve from the UserInfo the array of group names to apply.
   */
  public static final String GROUPS_JS_PARAM = "groupsJS";

  /**
   * Claim(s) to inspect for an array of group names to apply.
   */
  public static final String GROUPS_CLAIM_PARAM = "groupsClaim";

  private final OIDCConfiguration configuration;

  public OIDCRealm(OIDCConfiguration configuration) {
    setName(configuration.getName());
    this.configuration = configuration;
  }

  @Override
  public boolean supports(AuthenticationToken token) {
    return token instanceof OIDCAuthenticationToken;
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
    OIDCCredentials credentials = (OIDCCredentials) token.getCredentials();
    try {
      JWTClaimsSet claims = credentials.getIdToken().getJWTClaimsSet();
      String issuer = claims.getIssuer();
      if (!configuration.findProviderMetaData().getIssuer().toString().equals(issuer)) return null;
    } catch (ParseException e) {
      log.debug("Error while accessing the claims for OIDC realm {}", getName());
      return null;
    }
    String uname = credentials.getUsername();
    Map<String, Object> userInfo = credentials.getUserInfo();
    log.info("OIDC realm {}, user {} has UserInfo {}", getName(), uname, userInfo);
    List<Object> principals = Lists.newArrayList(uname);
    if (userInfo != null) {
      principals.add(userInfo);
    }
    final PrincipalCollection principalCollection = new SimplePrincipalCollection(principals, getName());
    return new SimpleAuthenticationInfo(principalCollection, token.getCredentials());
  }

  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
    Collection<?> thisPrincipals = principals.fromRealm(getName());

    if (thisPrincipals != null && !thisPrincipals.isEmpty()) {
      // groups to be automatically applied
      String groupsParam = configuration.getCustomParam(GROUPS_PARAM);
      Set<String> groups = Sets.newHashSet(getName());
      if (!Strings.isNullOrEmpty(groupsParam)) {
        extractGroups(groupsParam).forEach(groups::add);
      }
      // groups to be retrieved from user info claims
      for (Object principal : thisPrincipals) {
        if (principal instanceof Map) {
          newOIDCGroupsExtractor().extractGroups(configuration, (Map<String, Object>) principal)
              .stream()
              .map(String::trim)
              .filter(g -> !g.isEmpty())
              .forEach(groups::add);
        }
      }
      return new SimpleAuthorizationInfo(groups);
    }
    return new SimpleAuthorizationInfo();
  }

  protected OIDCGroupsExtractor newOIDCGroupsExtractor() {
    return new DefaultOIDCGroupsExtractor();
  }

  /**
   * Extract group names from a comma or space separated string.
   *
   * @param groupsParam
   * @return
   */
  protected Iterable<String> extractGroups(String groupsParam) {
    if (Strings.isNullOrEmpty(groupsParam)) {
      return Lists.newArrayList();
    } else if (groupsParam.contains(",")) {
      return Splitter.on(",").omitEmptyStrings().trimResults().split(groupsParam);
    } else {
      return Splitter.on(" ").omitEmptyStrings().trimResults().split(groupsParam);
    }
  }

}
