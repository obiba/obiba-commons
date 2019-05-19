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
