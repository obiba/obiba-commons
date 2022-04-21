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

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.CredentialsException;
import org.apache.shiro.mgt.SessionsSecurityManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.obiba.shiro.authc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import javax.annotation.Nullable;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);

  public static final String AUTHORIZATION_HEADER = "Authorization";

  public static final String AUTHORIZATION_BEARER_SCHEME = "Bearer";

  public static final String OBIBA_COOKIE_ID = "obibaid";

  @Autowired
  private SessionsSecurityManager securityManager;

  @Autowired
  private Environment environment;

  private String sessionIdCookieName;

  private String requestIdCookieName;

  private String headerCredentials;

  private String credentialsScheme;

  private String requestPrefix;

  private List<String> requestPrefixes;

  private String contextPath;

  @Autowired(required = false)
  private AuthenticationExecutor authenticationExecutor;

  @Value("${org.obiba.shiro.authenticationFilter.cookie.sessionId}")
  public void setSessionIdCookieName(String sessionIdCookieName) {
    this.sessionIdCookieName = sessionIdCookieName;
  }

  @Value("${org.obiba.shiro.authenticationFilter.cookie.requestId}")
  public void setRequestIdCookieName(String requestIdCookieName) {
    this.requestIdCookieName = requestIdCookieName;
  }

  @Value("${org.obiba.shiro.authenticationFilter.requestPrefix}")
  public void setRequestPrefix(String requestPrefix) {
    this.requestPrefix = requestPrefix;
  }

  @Value("${org.obiba.shiro.authenticationFilter.headerCredentials:X-Auth}")
  public void setHeaderCredentials(String headerCredentials) {
    this.headerCredentials = headerCredentials;
  }

  /**
   * Use <b>Basic</b> by default
   *
   * @param credentialsScheme
   */
  @Value("${org.obiba.shiro.authenticationFilter.credentialsScheme:Basic}")
  public void setCredentialsScheme(String credentialsScheme) {
    this.credentialsScheme = credentialsScheme;
  }

  public void setAuthenticationExecutor(AuthenticationExecutor authenticationExecutor) {
    this.authenticationExecutor = authenticationExecutor;
  }

  @NotNull
  private AuthenticationExecutor getAuthenticationExecutor() {
    if (authenticationExecutor == null) {
      authenticationExecutor = new AbstractAuthenticationExecutor() {
        @Override
        protected void ensureProfile(Subject subject) {
          // do nothing
        }
      };
    }
    return authenticationExecutor;
  }

  public void initContextPath() {
    // spring boot 1
    contextPath = environment.getProperty("server.context-path", "");
    // spring boot 2
    if (Strings.isNullOrEmpty(contextPath))
      contextPath = environment.getProperty("server.servlet.context-path", "");
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    if (requestPrefixes == null) {
      requestPrefixes = Splitter.on(",").splitToList(requestPrefix);
      initContextPath();
      if (!Strings.isNullOrEmpty(contextPath))
        requestPrefixes = requestPrefixes.stream().map(s -> contextPath + s).collect(Collectors.toList());
    }
    String requestUri = request.getRequestURI();
    if (!(contextPath + "/").equals(requestUri) && !requestPrefixes.isEmpty() && requestPrefixes.stream().noneMatch(requestUri::startsWith)) {
      filterChain.doFilter(request, response);
      return;
    }

    if (ThreadContext.getSubject() != null) {
      log.warn("Previous executing subject was not properly unbound from executing thread. Unbinding now.");
      ThreadContext.unbindSubject();
    }

    try {
      authenticateAndBind(request);
      filterChain.doFilter(request, response);
    } catch (CredentialsException e) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    } catch (AuthenticationException e) {
      if (log.isDebugEnabled())
        log.warn("Unexpected authentication error", e);
      else
        log.warn("Unexpected authentication error: {}", e.getMessage());
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    } catch (NoSuchOtpException e) {
      log.warn("OTP Exception ", e);
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setHeader("WWW-Authenticate", e.getOtpHeader());
    } catch (Exception e) {
      log.error("Exception ", e);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    } finally {
      unbind();
    }
  }

  /**
   * This method will try to authenticate the user using the provided sessionId or the "Authorization" header. When no
   * credentials are provided, this method does nothing. This will invoke the filter chain with an anonymous subject,
   * which allows fetching public web resources.
   *
   * @param request
   */
  private void authenticateAndBind(HttpServletRequest request) {

    Subject subject = authenticateSslCert(request);
    if (subject == null) {
      subject = authenticateAuthHeader(request);
    }
    if (subject == null) {
      subject = authenticateBasicHeader(request);
    }
    if (subject == null) {
      subject = authenticateCredentialsSchemeHeader(request);
    }
    if (subject == null) {
      subject = authenticateCookie(request);
    }
    if (subject == null) {
      subject = authenticateTicket(request);
    }
    if (subject == null) {
      subject = authenticateBearerHeader(request);
    }

    if (subject != null) {
      Session session = subject.getSession();
      log.trace("Binding subject {} session {} to executing thread {}", subject.getPrincipal(), session.getId(),
          Thread.currentThread().getId());
      ThreadContext.bind(subject);
      session.touch();
      log.debug("Successfully authenticated subject {}", SecurityUtils.getSubject().getPrincipal());
    }
  }

  @Nullable
  private Subject authenticateSslCert(HttpServletRequest request) {
    X509Certificate[] chain = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
    if (chain == null || chain.length == 0) return null;

    AuthenticationToken token = new X509CertificateAuthenticationToken(chain[0]);
    String sessionId = extractSessionId(request);
    Subject subject = new Subject.Builder(securityManager).sessionId(sessionId).buildSubject();
    try {
      subject.login(token);
    } catch (AuthenticationException e) {
      return null;
    }
    return subject;
  }

  @Nullable
  private Subject authenticateAuthHeader(HttpServletRequest request) {
    String authToken = request.getHeader(headerCredentials);
    if (authToken == null || authToken.isEmpty()) return null;

    String sessionId = extractSessionId(request);
    AuthenticationToken token = new HttpHeaderAuthenticationToken(authToken);
    try {
      return authenticateBasicHeader(request, token, sessionId);
    } catch (UnknownSessionException e) {
      // obiba/agate#302 if for any reason session cannot be retrieved, login with a new session
      return authenticateBasicHeader(request, token, null);
    }
  }

  @Nullable
  private Subject authenticateBasicHeader(HttpServletRequest request) {
    String authorization = request.getHeader(AUTHORIZATION_HEADER);
    if (authorization == null || authorization.isEmpty() || !authorization.startsWith("Basic "))
      return null;

    String sessionId = extractSessionId(request);
    AuthenticationToken token = new HttpAuthorizationToken("Basic", authorization);
    try {
      return authenticateBasicHeader(request, token, sessionId);
    } catch (UnknownSessionException e) {
      // obiba/agate#302 if for any reason session cannot be retrieved, login with a new session
      return authenticateBasicHeader(request, token, null);
    }
  }

  @Nullable
  private Subject authenticateCredentialsSchemeHeader(HttpServletRequest request) {
    String authorization = request.getHeader(AUTHORIZATION_HEADER);
    if (authorization == null || authorization.isEmpty() || !authorization.startsWith(credentialsScheme + " "))
      return null;

    String sessionId = extractSessionId(request);
    AuthenticationToken token = new HttpAuthorizationToken(credentialsScheme, authorization);
    try {
      return authenticateBasicHeader(request, token, sessionId);
    } catch (UnknownSessionException e) {
      // obiba/agate#302 if for any reason session cannot be retrieved, login with a new session
      return authenticateBasicHeader(request, token, null);
    }
  }

  @Nullable
  private Subject authenticateBasicHeader(HttpServletRequest request, AuthenticationToken token, String sessionId) {
    try {
      return getAuthenticationExecutor().login(request, token, sessionId);
    } catch (AuthenticationException e) {
      return null;
    }
  }

  @Nullable
  private Subject authenticateCookie(HttpServletRequest request) {
    Cookie sessionCookie = WebUtils.getCookie(request, sessionIdCookieName);
    Cookie requestCookie = WebUtils.getCookie(request, requestIdCookieName);
    if (isValid(sessionCookie)) {
      String sessionId = extractSessionId(request, sessionCookie);
      String requestId = requestCookie == null ? "" : requestCookie.getValue();
      try {
        return authenticateCookie(request, sessionId, requestId);
      } catch (UnknownSessionException e) {
        return authenticateCookie(request, null, requestId);
      }
    }
    return null;
  }

  @Nullable
  private Subject authenticateCookie(HttpServletRequest request, String sessionId, String requestId) {
    AuthenticationToken token = new HttpCookieAuthenticationToken(sessionId, request.getRequestURI(), requestId);
    Subject subject = new Subject.Builder(securityManager).sessionId(sessionId).buildSubject();
    try {
      subject.login(token);
    } catch (AuthenticationException e) {
      return null;
    }
    return subject;
  }

  /**
   * The ticket token ID is the obiba cookie.
   *
   * @param request
   * @return
   */
  @Nullable
  private Subject authenticateTicket(HttpServletRequest request) {
    Cookie ticketCookie = WebUtils.getCookie(request, OBIBA_COOKIE_ID);
    if (isValid(ticketCookie)) {
      String ticketId = ticketCookie.getValue();
      AuthenticationToken token = new TicketAuthenticationToken(ticketId, request.getRequestURI(), OBIBA_COOKIE_ID);
      try {
        return getAuthenticationExecutor().login(request, token);
      } catch (AuthenticationException e) {
        return null;
      }
    }
    return null;
  }

  /**
   * The ticket token ID is in the Authorization header with the "Bearer" scheme.
   *
   * @param request
   * @return
   */
  @Nullable
  private Subject authenticateBearerHeader(HttpServletRequest request) {
    String authorization = request.getHeader(AUTHORIZATION_HEADER);
    if (authorization == null || authorization.isEmpty()) return null;

    String schemeAndToken[] = authorization.split(" ", 2);
    if (schemeAndToken.length < 2) return null;
    if (!AUTHORIZATION_BEARER_SCHEME.equals(schemeAndToken[0])) return null;
    if (Strings.isNullOrEmpty(schemeAndToken[1])) return null;
    String ticketId = schemeAndToken[1];
    AuthenticationToken token = new TicketAuthenticationToken(ticketId, request.getRequestURI(), OBIBA_COOKIE_ID);
    try {
      return getAuthenticationExecutor().login(request, token);
    } catch (AuthenticationException e) {
      return null;
    }
  }

  private boolean isValid(Cookie cookie) {
    return cookie != null && cookie.getValue() != null;
  }

  private String extractSessionId(HttpServletRequest request) {
    return extractSessionId(request, null);
  }

  private String extractSessionId(HttpServletRequest request, @Nullable Cookie sessionCookie) {
    String sessionId = null;
    Cookie safeSessionCookie = sessionCookie == null
        ? WebUtils.getCookie(request, sessionIdCookieName)
        : sessionCookie;
    if (safeSessionCookie != null) {
      sessionId = safeSessionCookie.getValue();
    }
    return sessionId;
  }

  private void unbind() {
    try {
      if (log.isTraceEnabled()) {
        Subject s = ThreadContext.getSubject();
        if (s != null) {
          Session session = s.getSession(false);
          log.trace("Unbinding subject {} session {} from executing thread {}", s.getPrincipal(),
              session == null ? null : session.getId(), Thread.currentThread().getId());
        }
      }
    } finally {
      ThreadContext.unbindSubject();
    }
  }

}
