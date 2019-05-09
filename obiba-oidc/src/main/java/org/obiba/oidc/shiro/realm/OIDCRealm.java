package org.obiba.oidc.shiro.realm;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import org.apache.shiro.subject.SimplePrincipalCollection;
import org.obiba.oidc.shiro.authc.OIDCAuthenticationToken;

import java.util.List;

public class OIDCRealm extends AuthorizingRealm {

  public OIDCRealm(String name) {
    setName(name);
  }

  @Override
  public boolean supports(AuthenticationToken token) {
    return token instanceof OIDCAuthenticationToken;
  }


  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
    List<Object> principals = Lists.newArrayList(((OIDCAuthenticationToken) token).getUsername());
    final PrincipalCollection principalCollection = new SimplePrincipalCollection(principals, getName());
    return new SimpleAuthenticationInfo(principalCollection, token.getCredentials());
  }

  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
    return new SimpleAuthorizationInfo(Sets.newHashSet("opal-administrator"));
  }


}
