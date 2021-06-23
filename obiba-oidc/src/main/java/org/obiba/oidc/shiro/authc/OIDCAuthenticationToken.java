package org.obiba.oidc.shiro.authc;

import com.nimbusds.jwt.JWTClaimsSet;
import org.apache.shiro.authc.AuthenticationToken;
import org.obiba.oidc.OIDCCredentials;
import org.obiba.oidc.OIDCException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;

public class OIDCAuthenticationToken implements AuthenticationToken {

  private static final Logger log = LoggerFactory.getLogger(OIDCAuthenticationToken.class);

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
