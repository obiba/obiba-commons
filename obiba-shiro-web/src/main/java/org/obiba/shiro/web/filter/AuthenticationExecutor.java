package org.obiba.shiro.web.filter;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;

public interface AuthenticationExecutor {

  Subject login(AuthenticationToken token) throws AuthenticationException;

}
