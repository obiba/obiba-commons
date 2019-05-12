package org.obiba.oidc.shiro.authc;

import com.google.common.base.Strings;
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
    try {
      String uname = credentials.getIdToken().getJWTClaimsSet().getStringClaim("preferred_username");
      if (Strings.isNullOrEmpty(uname)) {
        uname = credentials.getIdToken().getJWTClaimsSet().getStringClaim("username");
      }
      if (Strings.isNullOrEmpty(uname)) {
        uname = credentials.getIdToken().getJWTClaimsSet().getSubject();
      }
      return uname;
    } catch (ParseException e) {
      return getPrincipal().toString();
    }
  }
}
