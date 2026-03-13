package org.obiba.shiro;

import com.google.common.base.Strings;
import org.apache.shiro.authc.AuthenticationException;

/**
 * Exception to raise when one-time password is missing from request headers.
 */
public class NoSuchOtpException extends AuthenticationException {

  private final String otpHeader;

  // OTP QR code
  private final String qrImage;

  // Whether a temporary code was sent by email
  private final boolean email;

  public NoSuchOtpException(String otpHeader) {
    this(otpHeader, null, false);
  }

  public NoSuchOtpException(String otpHeader, String qrImage, boolean email) {
    this.otpHeader = otpHeader;
    this.qrImage = qrImage;
    this.email = email;
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

  public boolean isEmail() {
    return email;
  }
}
