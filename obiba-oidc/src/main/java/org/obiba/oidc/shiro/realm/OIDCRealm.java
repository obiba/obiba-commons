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
import org.json.JSONArray;
import org.obiba.oidc.OIDCConfiguration;
import org.obiba.oidc.OIDCCredentials;
import org.obiba.oidc.shiro.authc.OIDCAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Realm based on OpenID connect token, after all authorization and identification stuff has happen.
 */
public class OIDCRealm extends AuthorizingRealm {

  private static final Logger log = LoggerFactory.getLogger(OIDCRealm.class);

  public static final String GROUPS_PARAM = "groups";

  public static final String GROUPS_CLAIM_PARAM = "groupsClaim";

  private static final String DEFAULT_GROUPS_CLAIM = "groups";

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
    Map<String, Object> userInfo = credentials.getUserInfo();
    log.info("OIDC realm {}, received userInfo {}", getName(), userInfo);
    String uname = ((OIDCAuthenticationToken) token).findUsername();
    if (Strings.isNullOrEmpty(uname) && userInfo != null) {
      // try different friendly user names
      if (userInfo.containsKey("preferred_username")) {
        uname = userInfo.get("preferred_username").toString();
      } else if (userInfo.containsKey("username")) {
        uname = userInfo.get("username").toString();
      } else if (userInfo.containsKey("email")) {
        // generally email are considered unique user identifiers
        uname = userInfo.get("email").toString();
      } else if (userInfo.containsKey("name")) {
        // make a user name from name
        uname = userInfo.get("name").toString().toLowerCase().replaceAll(" ", ".");
      }
    }
    // fallback: use subject ID from the JWT
    if (Strings.isNullOrEmpty(uname)) {
      uname = token.getPrincipal().toString();
    }
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
          try {
            // TODO improve by getting groups key from OIDC config
            Object gps = ((Map<String, Object>) principal).get(getGroupsClaim());
            if (gps != null) {
              extractGroups(gps).forEach(groups::add);
            }
          } catch (Exception e) {
            log.debug("Principal: {}", principal);
            log.warn("Failed at retrieving userInfo from principal", e);
          }
        }
      }
      return new SimpleAuthorizationInfo(groups);
    }
    return new SimpleAuthorizationInfo();
  }

  protected Iterable<String> extractGroups(Object groupsParam) {
    if (groupsParam instanceof Collection) {
      return ((Collection<Object>)groupsParam).stream()
          .map(Object::toString)
          .collect(Collectors.toSet());
    } else {
      String groupsParamStr = groupsParam.toString();
      if (groupsParamStr.startsWith("[") && groupsParamStr.endsWith("]")) {
        // expect a json array
        return new JSONArray(groupsParamStr).toList().stream()
            .filter(Objects::nonNull)
            .map(Object::toString)
            .collect(Collectors.toList());
      }
      return Splitter.on(" ").omitEmptyStrings().trimResults().split(groupsParamStr);
    }
  }


  protected String getGroupsClaim() {
    String groupsClaim = configuration.getCustomParam(GROUPS_CLAIM_PARAM);
    return Strings.isNullOrEmpty(groupsClaim) ? DEFAULT_GROUPS_CLAIM : groupsClaim;
  }
}
