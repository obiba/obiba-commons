/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.shiro.authc;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;

/**
 * {@code AuthorizationToken} for performing an action with elevated privileges.
 */
public class SudoAuthToken implements AuthenticationToken {

  private static final long serialVersionUID = 4956112777374283844L;

  private final PrincipalCollection sudoer;

  public SudoAuthToken(Subject sudoer) {
    this.sudoer = sudoer.getPrincipals();
  }

  @Override
  public Object getPrincipal() {
    return sudoer.getPrimaryPrincipal();
  }

  @Override
  public Object getCredentials() {
    return null;
  }

  public PrincipalCollection getSudoer() {
    return sudoer;
  }
}
