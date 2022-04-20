package org.obiba.shiro.web.filter;

/**
 * Exception to raise when one-time password is wrong.
 */
public class NoSuchOtpException extends RuntimeException {

  private final String otpStrategy;

  public NoSuchOtpException(String otpStrategy) {
    this.otpStrategy = otpStrategy;
  }

  public String getOtpStrategy() {
    return otpStrategy;
  }
}
