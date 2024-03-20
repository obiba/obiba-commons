package org.obiba.shiro;

import com.google.common.base.Strings;
import org.apache.shiro.authc.AuthenticationException;

/**
 * Exception to raise when one-time password is missing from request headers.
 */
public class NoSuchOtpException extends AuthenticationException {

  private final String otpHeader;

  private final String qrImage;

  public NoSuchOtpException(String otpHeader) {
    this(otpHeader, null);
  }

  public NoSuchOtpException(String otpHeader, String qrImage) {
    this.otpHeader = otpHeader;
    this.qrImage = qrImage;
  }

  public String getOtpHeader() {
    return otpHeader;
  }

  public boolean hasQrImage() {
    return !Strings.isNullOrEmpty(qrImage);
  }

  public String getQrImage() {
    return qrImage;
  }
}
