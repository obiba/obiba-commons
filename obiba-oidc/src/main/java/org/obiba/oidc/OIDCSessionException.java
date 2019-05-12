package org.obiba.oidc;

public class OIDCSessionException extends OIDCException {

  private final OIDCSession session;

  public OIDCSessionException(String message, OIDCSession session) {
    super(message);
    this.session = session;
  }

  public OIDCSession getSession() {
    return session;
  }

  public boolean hasSession() {
    return session != null;
  }

}
