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

import javax.annotation.Nullable;

import org.apache.shiro.authc.AuthenticationToken;

/**
 * Personal API access token that both identifies and authenticate the user.
 */
public class HttpHeaderAuthenticationToken implements AuthenticationToken {

  private static final long serialVersionUID = 4520790559763117320L;

  private final String token;

  public HttpHeaderAuthenticationToken(String token) {
    this.token = token;
  }

  @Override
  public Object getPrincipal() {
    return getToken();
  }

  @Nullable
  @Override
  public Object getCredentials() {
    return getToken();
  }

  public String getToken() {
    return token;
  }

}
