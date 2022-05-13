package org.obiba.shiro;

import org.apache.shiro.authc.AuthenticationException;

/**
 * Exception to raise when one-time password is missing from request headers.
 */
public class NoSuchOtpException extends AuthenticationException {

  private final String otpHeader;

  public NoSuchOtpException(String otpHeader) {
    this.otpHeader = otpHeader;
  }

  public String getOtpHeader() {
    return otpHeader;
  }
}
