package org.obiba.shiro.web.filter;

/**
 * Exception to raise when one-time password is wrong.
 */
public class NoSuchOtpException extends RuntimeException {

  private final String otpStrategy;

  private final String otpHeader;

  public NoSuchOtpException(String otpStrategy) {
    this(otpStrategy, "");
  }

  public NoSuchOtpException(String otpStrategy, String headerPrefix) {
    this.otpStrategy = otpStrategy;
    this.otpHeader = headerPrefix + otpStrategy;
  }

  public String getOtpStrategy() {
    return otpStrategy;
  }

  public String getOtpHeader() {
    return otpHeader;
  }
}
