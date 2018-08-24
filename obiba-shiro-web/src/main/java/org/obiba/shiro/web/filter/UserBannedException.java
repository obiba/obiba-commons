package org.obiba.shiro.web.filter;

import org.apache.shiro.authc.AuthenticationException;

public class UserBannedException extends AuthenticationException {

    public UserBannedException(String message) {
        super(message);
    }

}
