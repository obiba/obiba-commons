/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.shiro.web.filter;


import jakarta.servlet.http.HttpServletRequest;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.springframework.lang.Nullable;

/**
 * Implement this interface so that the AuthenticationFilter handles the single sign-on ticket.
 */
public interface AuthenticationExecutor {

  /**
   * Performs the shiro login.
   *
   * @param token
   * @return
   * @throws AuthenticationException
   */
  @Nullable
  default Subject login(AuthenticationToken token) throws AuthenticationException {
    return login(null, token, null);
  }

  /**
   * Performs the shiro login in an HTTP context.
   *
   * @param request
   * @param token
   * @return
   * @throws AuthenticationException
   */
  @Nullable
  default Subject login(HttpServletRequest request, AuthenticationToken token) throws AuthenticationException {
    return login(request, token, null);
  }

  /**
   * Reuse a session and login.
   *
   * @param request
   * @param token
   * @param sessionId
   * @return
   * @throws AuthenticationException
   */
  @Nullable
  Subject login(HttpServletRequest request, AuthenticationToken token, String sessionId) throws AuthenticationException;

  /**
   * Provides the context path if any is defined. Not necessary when using a Spring boot app.
   *
   * @return
   */
  default String getContextPath() {
    return "";
  }
}
