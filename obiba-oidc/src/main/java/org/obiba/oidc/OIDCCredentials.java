/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.oidc;

import com.nimbusds.jwt.JWT;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.RefreshToken;

import java.util.Map;

public class OIDCCredentials {

  private AuthorizationCode authorizationCode;

  private AccessToken accessToken;

  private RefreshToken refreshToken;

  private JWT idToken;

  private Map<String, Object> userInfo;

  public void setAuthorizationCode(AuthorizationCode authorizationCode) {
    this.authorizationCode = authorizationCode;
  }

  public AuthorizationCode getAuthorizationCode() {
    return authorizationCode;
  }

  public void setAccessToken(AccessToken accessToken) {
    this.accessToken = accessToken;
  }

  public AccessToken getAccessToken() {
    return accessToken;
  }

  public void setRefreshToken(RefreshToken refreshToken) {
    this.refreshToken = refreshToken;
  }

  public RefreshToken getRefreshToken() {
    return refreshToken;
  }

  public void setIdToken(JWT idToken) {
    this.idToken = idToken;
  }

  public JWT getIdToken() {
    return idToken;
  }

  public void setUserInfo(Map<String, Object> claims) {
    this.userInfo = claims;
  }

  public Map<String, Object> getUserInfo() {
    return userInfo;
  }

  public Object getUserInfo(String key) {
    return userInfo == null ? null : userInfo.get(key);
  }
}
