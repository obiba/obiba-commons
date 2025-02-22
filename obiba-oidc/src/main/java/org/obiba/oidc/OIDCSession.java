package org.obiba.oidc;

import com.google.common.base.Strings;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.openid.connect.sdk.Nonce;
import org.obiba.oidc.web.J2EContext;

import java.util.Map;

/**
 * An OpenID Connect session persists the some information from the login request processing to be validated during callback request processing.
 */
public class OIDCSession {

  // Unique identifier of the remote client
  private final String id;

  // The original State
  private final State state;

  // The original Nonce
  private final Nonce nonce;

  // The original request parameters
  private final Map<String, String[]> requestParameters;

  private String callbackError;

  public OIDCSession(J2EContext context, State state, Nonce nonce) {
    this(context.getClientId(), state, nonce, context.getRequestParameters());
  }

  public OIDCSession(String id, State state, Nonce nonce, Map<String, String[]> requestParameters) {
    this.id = id;
    this.state = state;
    this.nonce = nonce;
    this.requestParameters = requestParameters;
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

  public String getStateValue() {
    return hasState() ? state.getValue() : null;
  }

  public boolean hasNonce() {
    return nonce != null;
  }

  public Nonce getNonce() {
    return nonce;
  }

  public boolean hasRequestParameters() {
    return requestParameters != null && !requestParameters.isEmpty();
  }

  public Map<String, String[]> getRequestParameters() {
    return requestParameters;
  }

  public String getCallbackError() {
    return callbackError;
  }

  public void setCallbackError(String callbackError) {
    this.callbackError = callbackError;
  }

  public boolean hasCallbackError() {
    return !Strings.isNullOrEmpty(callbackError);
  }
}
