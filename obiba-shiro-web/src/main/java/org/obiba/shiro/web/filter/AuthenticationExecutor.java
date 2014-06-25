package org.obiba.shiro.web.filter;

import javax.annotation.Nullable;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;

/**
 * Implement this interface so that the AuthenticationFilter handles the single sign-on ticket.
 */
public interface AuthenticationExecutor {

  /**
   * Performs the shiro login.
   * @param token
   * @return
   * @throws AuthenticationException
   */
  @Nullable
  Subject login(AuthenticationToken token) throws AuthenticationException;

  /**
   * Reuse a session and login.
   * @param token
   * @param sessionId
   * @return
   * @throws AuthenticationException
   */
  @Nullable
  Subject login(AuthenticationToken token, String sessionId) throws AuthenticationException;

}
