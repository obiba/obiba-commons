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

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.mgt.SessionsSecurityManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionException;
import org.apache.shiro.session.mgt.DefaultSessionKey;
import org.apache.shiro.session.mgt.SessionKey;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.springframework.lang.Nullable;

public abstract class AbstractHttpAuthenticatingRealm extends AuthorizingRealm {

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
    Session session = getSession(getSessionId(token));
    if(session == null) {
      throw new IncorrectCredentialsException();
    }
    // Extract the principals from the session
    PrincipalCollection principals = (PrincipalCollection) session
        .getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY);
    if(principals != null) {
      return createAuthenticationInfo(token, principals);
    }
    throw new AuthenticationException();
  }

  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
    return null;
  }

  abstract protected AuthenticationInfo createAuthenticationInfo(AuthenticationToken token,
      PrincipalCollection principals);

  abstract protected String getSessionId(AuthenticationToken token);

  @Nullable
  protected Session getSession(String sessionId) {
    if(sessionId != null) {
      SessionManager manager = getSessionManager();
      if(manager != null) {
        SessionKey key = new DefaultSessionKey(sessionId);
        try {
          return manager.getSession(key);
        } catch(SessionException e) {
          // Means that the session does not exist or has expired.
        }
      }
    }
    return null;
  }

  @Nullable
  protected SessionManager getSessionManager() {
    SecurityManager sm = SecurityUtils.getSecurityManager();
    return sm instanceof SessionsSecurityManager ? sm : null;
  }
}
