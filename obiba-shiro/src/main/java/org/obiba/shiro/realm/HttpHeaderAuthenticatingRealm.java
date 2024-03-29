/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.shiro.realm;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAccount;
import org.apache.shiro.authc.credential.AllowAllCredentialsMatcher;
import org.apache.shiro.subject.PrincipalCollection;
import org.obiba.shiro.authc.HttpHeaderAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class HttpHeaderAuthenticatingRealm extends AbstractHttpAuthenticatingRealm {

  public HttpHeaderAuthenticatingRealm() {
    setCredentialsMatcher(new AllowAllCredentialsMatcher());
    setAuthenticationTokenClass(HttpHeaderAuthenticationToken.class);
  }
  
  @Override
  protected String getSessionId(AuthenticationToken token) {
    return ((HttpHeaderAuthenticationToken) token).getToken();
  }

  @Override
  protected AuthenticationInfo createAuthenticationInfo(AuthenticationToken token, PrincipalCollection principals) {
    return new SimpleAccount(principals, null);
  }

}
