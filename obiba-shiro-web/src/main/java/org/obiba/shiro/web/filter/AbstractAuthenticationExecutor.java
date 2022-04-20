/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.shiro.web.filter;

import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public abstract class AbstractAuthenticationExecutor implements AuthenticationExecutor {

  private static final Logger log = LoggerFactory.getLogger(AbstractAuthenticationExecutor.class);

  private Map<String, List<Date>> loginFailures = Maps.newConcurrentMap();

  private Cache<String, Date> banCache;

  /**
   * Number of attempts before being banned.
   */
  private int maxTry = 3;

  /**
   * Time span in which the maximum of tries count should happen before starting a ban period, in seconds.
   */
  private int trialTime = 300;

  /**
   * Ban period after max retry was reached, in seconds.
   */
  private int banTime = 300;

  @Override
  public Subject login(HttpServletRequest request, AuthenticationToken token, String sessionId) throws AuthenticationException {
    if (isBanEnabled() && token instanceof UsernamePasswordToken) {
      UsernamePasswordToken uToken = (UsernamePasswordToken) token;
      if (banCache.getIfPresent(uToken.getUsername()) != null)
        throwUserBannedException(uToken);
    }
    Subject subject = createSubjectFromSessionId(sessionId);

    if (!subject.isAuthenticated()) {

      // subject is not authenticated but has a sessionId? delete the session
      if (sessionId != null) {
        try {
          subject.logout();
        } catch (Exception e) {
          // ignore, caused by an invalid session
        }

        // work with a new subject, same sessionId
        subject = createSubjectFromSessionId(sessionId);
      }

      try {
        if (request != null)
          processRequest(request, token);
        subject.login(token);
        ThreadContext.bind(subject);
        // successful login, so reset the failures list
        if (isBanEnabled() && token instanceof UsernamePasswordToken) {
          UsernamePasswordToken uToken = (UsernamePasswordToken) token;
          loginFailures.remove(uToken.getUsername());
        }
        ensureProfile(subject);
      } catch (AuthenticationException e) {
        onLoginFailure(token);
        throw e;
      }
    }
    return subject.isAuthenticated() ? subject : null;
  }

  /**
   * Process the HTTP request, for retrieving a one-time token.
   *
   * @param request
   */
  protected void processRequest(HttpServletRequest request, AuthenticationToken token) {
    // do nothing
  }

  /**
   * Trigger some processing after the login evaluation.
   *
   * @param subject
   */
  protected abstract void ensureProfile(Subject subject);

  /**
   * Configure the user ban check.
   *
   * @param maxTry    Maximum count of failed logins
   * @param trialTime Time period during which the maximum of tries were recorded. No time limit if not positive
   * @param banTime   Ban time, enabled if positive
   */
  protected void configureBan(int maxTry, int trialTime, int banTime) {
    this.maxTry = maxTry;
    this.trialTime = trialTime;
    this.banTime = banTime;
    if (banTime > 0) {
      banCache = CacheBuilder.newBuilder()
          .expireAfterWrite(banTime, TimeUnit.SECONDS)
          .build();
    }
  }

  /**
   * User ban is enabled if it was explicitly configured.
   *
   * @return
   */
  private boolean isBanEnabled() {
    return banCache != null;
  }

  private Subject createSubjectFromSessionId(String sessionId) {
    if (!Strings.isNullOrEmpty(sessionId)) {
      return new Subject.Builder(SecurityUtils.getSecurityManager()).sessionId(sessionId).buildSubject();
    } else {
      return SecurityUtils.getSubject();
    }
  }

  /**
   * If it is a user name based authentication token, apply ban check on login failure (if enabled).
   *
   * @param token
   */
  private synchronized void onLoginFailure(AuthenticationToken token) {
    if (!isBanEnabled()) return;
    if (token instanceof UsernamePasswordToken) {
      UsernamePasswordToken uToken = (UsernamePasswordToken) token;

      List<Date> failures = loginFailures.getOrDefault(uToken.getUsername(), Lists.newArrayList());
      failures.add(new Date());
      loginFailures.put(uToken.getUsername(), failures);
      log.warn("Login failed for user '{}' [{}]", uToken.getUsername(), loginFailures.get(uToken.getUsername()).size());
      if (isToBeBanned(failures)) {
        loginFailures.remove(uToken.getUsername());
        banCache.put(uToken.getUsername(), new Date());
        throwUserBannedException(uToken);
      }
    }
  }

  /**
   * User can be banned if the max count of tries is reached and if these tries were made during a certain positive amount of time.
   *
   * @param failures
   * @return
   */
  private boolean isToBeBanned(List<Date> failures) {
    if (failures.size() < maxTry) return false;
    if (trialTime <= 0) return true;
    Date firstFailure = failures.get(failures.size() - maxTry);
    Date lastFailure = failures.get(failures.size() - 1);
    return (lastFailure.getTime() - firstFailure.getTime()) <= (trialTime * 1000);
  }

  /**
   * Inform about the ban situation.
   *
   * @param uToken
   */
  private void throwUserBannedException(UsernamePasswordToken uToken) {
    Date banDate = banCache.getIfPresent(uToken.getUsername());
    int remainingBanTime = banTime - (int) (new Date().getTime() - banDate.getTime()) / 1000;
    log.warn("User '{}' is banned for a duration of {}s", uToken.getUsername(), remainingBanTime);
    throw new UserBannedException("User is banned: " + uToken.getUsername(), uToken.getUsername(), remainingBanTime);
  }

}
