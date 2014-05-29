package org.obiba.shiro.web.filter;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;

/**
 * Implement this interface so that the AuthenticationFilter handles the single sign-on ticket.
 */
public interface AuthenticationExecutor {

  Subject login(AuthenticationToken token) throws AuthenticationException;

}
