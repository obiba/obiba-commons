/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.shiro.authc;


import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.pam.UnsupportedTokenException;
import java.util.Base64;
import org.springframework.lang.Nullable;

/**
 *
 */
public class HttpAuthorizationToken extends UsernamePasswordOtpToken implements AuthenticationToken {

  private static final long serialVersionUID = 4520790559763117320L;

  public HttpAuthorizationToken(String scheme, String authorization) {
    this(scheme, authorization, null, null);
  }

  public HttpAuthorizationToken(String scheme, String authorization, String otp) {
    this(scheme, authorization, null, otp);
  }

  private HttpAuthorizationToken(String scheme, String authorization, @Nullable String host, String otp) {
    this(new DecodedCredentials(scheme, authorization), host, otp);
  }

  private HttpAuthorizationToken(DecodedCredentials decoded, @Nullable String host, String otp) {
    super(decoded.getUsername(), decoded.getPassword(), host, otp);
  }

  private static class DecodedCredentials {

    private final String username;

    private final String password;

    DecodedCredentials(String scheme, String authorization) throws UnsupportedTokenException {
      // Scheme <token>
      String schemeAndToken[] = authorization.split(" ", 2);

      if(!scheme.equals(schemeAndToken[0])) {
        throw new UnsupportedTokenException();
      }

      String decoded[] = new String(Base64.getDecoder().decode(schemeAndToken[1])).split(":", 2);
      username = decoded[0];
      password = decoded[1];
    }

    public String getUsername() {
      return username;
    }

    public String getPassword() {
      return password;
    }
  }

}
