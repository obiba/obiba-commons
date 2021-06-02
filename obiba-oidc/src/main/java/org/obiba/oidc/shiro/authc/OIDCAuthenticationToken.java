package org.obiba.oidc.shiro.authc;

import com.google.common.base.Strings;
import com.nimbusds.jwt.JWTClaimsSet;
import org.apache.shiro.authc.AuthenticationToken;
import org.obiba.oidc.OIDCCredentials;
import org.obiba.oidc.OIDCException;

import java.text.ParseException;

public class OIDCAuthenticationToken implements AuthenticationToken {

  private final OIDCCredentials credentials;

  public OIDCAuthenticationToken(OIDCCredentials credentials) {
    this.credentials = credentials;
  }

  @Override
  public Object getPrincipal() {
    try {
      return credentials.getIdToken().getJWTClaimsSet().getSubject();
    } catch (ParseException e) {
      throw new OIDCException("ID token cannot provide subject name");
    }
  }

  @Override
  public Object getCredentials() {
    return credentials;
  }

  public String getUsername() {
    String uname = null;
    try {
      JWTClaimsSet claimsSet = credentials.getIdToken().getJWTClaimsSet();
      uname = getStringClaim(claimsSet, "preferred_username");
      if (Strings.isNullOrEmpty(uname)) {
        uname = getStringClaim(claimsSet, "username");
      }
      if (Strings.isNullOrEmpty(uname)) {
        uname = getStringClaim(claimsSet, "name");
      }
      if (Strings.isNullOrEmpty(uname)) {
        uname = claimsSet.getSubject();
      }
    } catch (ParseException e) {
      // empty
    }
    return Strings.isNullOrEmpty(uname) ? getPrincipal().toString() : uname;
  }

  //
  // Private methods
  //

  private String getStringClaim(JWTClaimsSet claimsSet, String key) {
    try {
      return claimsSet.getStringClaim(key);
    } catch (ParseException e) {
      return null;
    }
  }
}
