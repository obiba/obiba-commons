package org.obiba.runtime.upgrade;

public class UpgradeException extends Exception {

  private static final long serialVersionUID = -5972984899869970156L;

  public UpgradeException(Exception cause) {
    super(cause);
  }
}
