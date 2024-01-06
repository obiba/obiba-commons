/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.crypt;

import java.io.IOException;
import java.util.Arrays;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 *
 */
public class FixedPasswordCallbackHandler implements CallbackHandler {

  private final char[] password;

  public FixedPasswordCallbackHandler(char... password) {
    this.password = Arrays.copyOf(password, password.length);
  }

  @Override
  public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
    for(Callback callback1 : callbacks) {
      if(callback1 instanceof PasswordCallback callback) {
        callback.setPassword(password);
        return;
      }
      throw new UnsupportedCallbackException(callback1);
    }
  }

}
