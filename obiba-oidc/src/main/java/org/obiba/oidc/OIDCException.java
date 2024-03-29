package org.obiba.oidc;

public class OIDCException extends RuntimeException {

  public OIDCException(Exception e) {
    super(e);
  }

  public OIDCException(String message) {
    super(message);
  }

  public OIDCException(String message, Exception cause) {
    super(message, cause);
  }
}
