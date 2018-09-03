package org.obiba.shiro.web.filter;

import org.apache.shiro.authc.AuthenticationException;

public class UserBannedException extends AuthenticationException {

    private final String user;

    private final int banTime;

    public UserBannedException(String message, String user, int banTime) {
        super(message);
        this.user = user;
        this.banTime = banTime;
    }

    public String getUser() {
        return user;
    }

    public int getBanTime() {
        return banTime;
    }
}
