package org.obiba.shiro.web.filter;

import org.apache.shiro.authc.AuthenticationException;

public class UserBannedException extends AuthenticationException {

    private final String user;

    private final int remainingBanTime;

    public UserBannedException(String message, String user, int remainingBanTime) {
        super(message);
        this.user = user;
        this.remainingBanTime = remainingBanTime;
    }

    public String getUser() {
        return user;
    }

    public int getRemainingBanTime() {
        return remainingBanTime;
    }
}
