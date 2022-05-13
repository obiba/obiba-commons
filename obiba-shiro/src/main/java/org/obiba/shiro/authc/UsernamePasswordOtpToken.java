package org.obiba.shiro.authc;

import com.google.common.base.Strings;
import org.apache.shiro.authc.UsernamePasswordToken;

/**
 * Username and password credentials, with a one-time password.
 */
public class UsernamePasswordOtpToken extends UsernamePasswordToken {

  private final String otp;

  public UsernamePasswordOtpToken(String username, String password, String host, String otp) {
    super(username, password, host);
    this.otp = otp;
  }

  public UsernamePasswordOtpToken(String username, String password, String otp) {
    super(username, password);
    this.otp = otp;
  }

  public String getOtp() {
    return otp;
  }

  public boolean hasOtp() {
    return !Strings.isNullOrEmpty(otp);
  }
}
