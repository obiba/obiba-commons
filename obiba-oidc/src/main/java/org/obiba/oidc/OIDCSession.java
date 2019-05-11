package org.obiba.oidc;

import com.google.common.base.Strings;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.openid.connect.sdk.Nonce;

/**
 * An OpenID Connect session persists the some information from the login request processing to be validated during callback request processing.
 */
public class OIDCSession {

  private final String id;

  private final State state;

  private final Nonce nonce;

  private final String redirectUri;

  public OIDCSession(String id, State state, Nonce nonce, String redirectUri) {
    this.id = id;
    this.state = state;
    this.nonce = nonce;
    this.redirectUri = redirectUri;
  }

  public String getId() {
    return id;
  }

  public boolean hasState() {
    return state != null;
  }

  public State getState() {
    return state;
  }

  public boolean hasNonce() {
    return nonce != null;
  }

  public Nonce getNonce() {
    return nonce;
  }

  public boolean hasRedirectUri() {
    return !Strings.isNullOrEmpty(redirectUri);
  }

  public String getRedirectUri() {
    return redirectUri;
  }
}
