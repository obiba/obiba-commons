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

import com.google.common.base.Strings;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Map;

public class OIDCCredentials {

  private static final Logger log = LoggerFactory.getLogger(OIDCCredentials.class);

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
    log.trace("Access token: {}", accessToken);
    this.accessToken = accessToken;
  }

  public AccessToken getAccessToken() {
    return accessToken;
  }

  public void setRefreshToken(RefreshToken refreshToken) {
    log.trace("Refresh token: {}", refreshToken);
    this.refreshToken = refreshToken;
  }

  public RefreshToken getRefreshToken() {
    return refreshToken;
  }

  public void setIdToken(JWT idToken) {
    try {
      log.trace("ID token claims: {}", idToken.getJWTClaimsSet());
    } catch (ParseException e) {
      log.warn("ID token claims are not accessible");
    }
    this.idToken = idToken;
  }

  public JWT getIdToken() {
    return idToken;
  }

  public void setUserInfo(Map<String, Object> claims) {
    log.trace("UserInfo: {}", claims);
    this.userInfo = claims;
  }

  public Map<String, Object> getUserInfo() {
    return userInfo;
  }

  public Object getUserInfo(String key) {
    return userInfo == null ? null : userInfo.get(key);
  }

  public String getUsername(String usernameClaim) {
    String uname = findUsernameInIdToken(usernameClaim);
    if (Strings.isNullOrEmpty(uname) && userInfo != null) {
      log.debug("Looking for username in userInfo {}", userInfo);
      // try different friendly user names
      if (!Strings.isNullOrEmpty(usernameClaim) && userInfo.containsKey(usernameClaim))
        uname = userInfo.get(usernameClaim).toString();
      if (Strings.isNullOrEmpty(uname) && userInfo.containsKey("preferred_username"))
        uname = userInfo.get("preferred_username").toString();
      if (Strings.isNullOrEmpty(uname) && userInfo.containsKey("username"))
        uname = userInfo.get("username").toString();
      // generally email are considered unique user identifiers
      if (Strings.isNullOrEmpty(uname) && userInfo.containsKey("email"))
        uname = userInfo.get("email").toString();
      // make a user name from name
      if (Strings.isNullOrEmpty(uname) && userInfo.containsKey("name"))
        uname = userInfo.get("name").toString().toLowerCase().replaceAll(" ", ".");
    }
    // fallback: use subject ID from the JWT
    if (Strings.isNullOrEmpty(uname)) {
      try {
        uname = idToken.getJWTClaimsSet().getSubject();
      } catch (ParseException e) {
        throw new OIDCException("No subject ID in JWT", e);
      }
    }
    return uname;
  }

  /**
   * Try to get the username from the JWT custom claims.
   *
   * @param usernameClaim
   * @return null if not found
   */
  private String findUsernameInIdToken(String usernameClaim) {
    try {
      JWTClaimsSet claimsSet = idToken.getJWTClaimsSet();
      log.debug("Looking for username in JWT claims: {}", claimsSet);
      String uname = null;
      if (!Strings.isNullOrEmpty(usernameClaim))
        uname = claimsSet.getStringClaim(usernameClaim);
      if (Strings.isNullOrEmpty(uname))
        uname = claimsSet.getStringClaim("preferred_username");
      if (Strings.isNullOrEmpty(uname))
        uname = claimsSet.getStringClaim("username");
      if (Strings.isNullOrEmpty(uname))
        uname = claimsSet.getStringClaim("email");
      return uname;
    } catch (ParseException e) {
      return null;
    }
  }
}
