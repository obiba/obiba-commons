package org.obiba.shiro.realm;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeSocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.AllowAllCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.obiba.shiro.authc.TicketAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

/**
 * A realm for the CAS-like implementation protocol by Obiba.
 */
public class ObibaRealm extends AuthorizingRealm {

  private final static Logger log = LoggerFactory.getLogger(ObibaRealm.class);

  public static final String OBIBA_REALM = "obiba-realm";

  public static final String TICKET_COOKIE_NAME = "obibaid";

  public static final String DEFAULT_REST_PREFIX = "/ws";

  public static final String DEFAULT_LOGIN_PATH = "/tickets";

  public static final String DEFAULT_TICKET_PATH = "/ticket/{id}";

  public static final String DEFAULT_VALIDATE_PATH = DEFAULT_TICKET_PATH + "/username";

  public static final String DEFAULT_SUBJECT_PATH = DEFAULT_TICKET_PATH + "/subject";

  private static final String SET_COOKIE_HEADER = "Set-Cookie";

  private static final int DEFAULT_HTTPS_PORT = 443;

  private HttpComponentsClientHttpRequestFactory httpRequestFactory;

  private String baseUrl = "https://localhost:8444";

  private String serviceName;

  private String serviceKey;

  public ObibaRealm() {
    super(null, new AllowAllCredentialsMatcher());
  }

  @Override
  public boolean supports(AuthenticationToken token) {
    return token != null && (UsernamePasswordToken.class.isAssignableFrom(token.getClass()) ||
        TicketAuthenticationToken.class.isAssignableFrom(token.getClass()));
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
    if(UsernamePasswordToken.class.isAssignableFrom(token.getClass()))
      return doGetUsernameAuthenticationInfo((UsernamePasswordToken) token);
    else return doGetTicketAuthenticationInfo((TicketAuthenticationToken) token);
  }

  private AuthenticationInfo doGetUsernameAuthenticationInfo(UsernamePasswordToken token)
      throws AuthenticationException {
    String username = token.getUsername();

    // Null username is invalid
    if(Strings.isNullOrEmpty(username)) {
      throw new AccountException("Empty usernames are not allowed by this realm.");
    }

    try {
      RestTemplate template = newRestTemplate();
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
      String form = "username=" + username + "&password=" + new String(token.getPassword());
      HttpEntity<String> entity = new HttpEntity<String>(form, headers);
      ResponseEntity<String> response = template.exchange(getLoginUrl(token), HttpMethod.POST, entity, String.class);

      if(response.getStatusCode().equals(HttpStatus.CREATED)) {
        HttpHeaders responseHeaders = response.getHeaders();
        for(String cookieValue : responseHeaders.get(SET_COOKIE_HEADER)) {
          if(cookieValue.startsWith(TICKET_COOKIE_NAME + "=")) {
            // set in the subject's session the cookie that will allow to perform the single sign-on
            SecurityUtils.getSubject().getSession().setAttribute(SET_COOKIE_HEADER, cookieValue);
            // keep ticket reference for logout
            String ticketId = cookieValue.split(";")[0].substring(TICKET_COOKIE_NAME.length() + 1);
            SecurityUtils.getSubject().getSession().setAttribute(TICKET_COOKIE_NAME, ticketId);
          }
        }

        return new SimpleAuthenticationInfo(username, token.getCredentials(), getName());
      }

      // not an account in this realm
      return null;
    } catch(HttpClientErrorException e) {
      return null;
    } catch(Exception e) {
      log.error("Auth failure: {}", e.getMessage(), e);
      throw new AuthenticationException("Failed authenticating on " + baseUrl, e);
    }
  }

  private AuthenticationInfo doGetTicketAuthenticationInfo(TicketAuthenticationToken token)
      throws AuthenticationException {
    // Null ticket id is invalid
    if(Strings.isNullOrEmpty(token.getTicketId())) {
      throw new AccountException("Empty tickets are not allowed by this realm.");
    }

    try {
      RestTemplate template = newRestTemplate();
      ResponseEntity<String> response = template.getForEntity(getValidateUrl(token.getTicketId()), String.class);

      if(response.getStatusCode().equals(HttpStatus.OK)) {
        // keep ticket reference for logout
        SecurityUtils.getSubject().getSession().setAttribute(TICKET_COOKIE_NAME, token.getTicketId());
        return new SimpleAuthenticationInfo(response.getBody(), token.getCredentials(), getName());
      }

      // not an account in this realm
      return null;
    } catch(HttpClientErrorException e) {
      return null;
    } catch(Exception e) {
      throw new AuthenticationException("Failed authenticating on " + baseUrl, e);
    }
  }

  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
    Collection<?> thisPrincipals = principals.fromRealm(getName());
    if(thisPrincipals != null && !thisPrincipals.isEmpty()) {
      try {
        RestTemplate template = newRestTemplate();
        ResponseEntity<Subject> response = template.getForEntity(getSubjectUrl(getTicketFromSession()), Subject.class);
        if(response.getStatusCode().equals(HttpStatus.OK)) {
          return new SimpleAuthorizationInfo(Sets.newHashSet(response.getBody().groups));
        }
      } catch(HttpClientErrorException e) {
        return new SimpleAuthorizationInfo();
      } catch(Exception e) {
        throw new AuthenticationException("Failed authorizing on " + baseUrl, e);
      }
    }
    return new SimpleAuthorizationInfo();
  }

  @Override
  public void onLogout(PrincipalCollection principals) {
    if (principals.getRealmNames().contains(OBIBA_REALM)) {
      cleanTicket();
    }
    super.onLogout(principals);
  }

  private void cleanTicket() {
    try {
      String ticketId = getTicketFromSession();
      if (ticketId != null) {
        log.debug("Deleting ticket: {}", ticketId);
        RestTemplate template = newRestTemplate();
        template.delete(getTicketUrl(ticketId));
      }
    } catch(Exception e) {
      log.warn("Unable to clean Obiba session: " + e.getMessage(), e);
    }
  }

  /**
   * Extract ticket reference from the shiro session.
   * @return null if not found
   */
  @Nullable
  private String getTicketFromSession() {
    Object cookie = SecurityUtils.getSubject().getSession().getAttribute(TICKET_COOKIE_NAME);
    return cookie != null && !Strings.isNullOrEmpty(cookie.toString()) ? cookie.toString() : null;
  }

  /**
   * Base url of Agate application.
   *
   * @param baseUrl
   */
  public void setBaseUrl(String baseUrl) {
    if(baseUrl.endsWith("/")) {
      this.baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
    } else {
      this.baseUrl = baseUrl;
    }
  }

  /**
   * Service name issuing credentials requests.
   *
   * @param service
   */
  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  /**
   * Service key issuing credentials requests.
   *
   * @param service
   */
  public void setServiceKey(String serviceKey) {
    this.serviceKey = serviceKey;
  }

  @Override
  public String getName() {
    return OBIBA_REALM;
  }

  private RestTemplate newRestTemplate() {
    log.info("Connecting to Agate: {}", baseUrl);
    if (baseUrl.toLowerCase().startsWith("https://")) {
      if(httpRequestFactory == null) {
        httpRequestFactory = new HttpComponentsClientHttpRequestFactory(createHttpClient());
      }
      return new RestTemplate(httpRequestFactory);
    } else {
      return new RestTemplate();
    }
  }

  private HttpClient createHttpClient() {
    DefaultHttpClient httpClient = new DefaultHttpClient();
    try {
      httpClient.getConnectionManager().getSchemeRegistry()
          .register(new Scheme("https", DEFAULT_HTTPS_PORT, getSocketFactory()));
    } catch(NoSuchAlgorithmException | KeyManagementException e) {
      throw new RuntimeException(e);
    }

    return httpClient;
  }

  /**
   * Do not check anything from the remote host (Agate server is trusted).
   * @return
   * @throws NoSuchAlgorithmException
   * @throws KeyManagementException
   */
  private SchemeSocketFactory getSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
    // Accepts any SSL certificate
    TrustManager tm = new X509TrustManager() {

      @Override
      public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {

      }

      @Override
      public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {

      }

      @Override
      public X509Certificate[] getAcceptedIssuers() {
        return null;
      }
    };
    SSLContext sslContext = SSLContext.getInstance("TLS");
    sslContext.init(null, new TrustManager[] { tm }, null);

    return new SSLSocketFactory(sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
  }

  private String getLoginUrl(UsernamePasswordToken token) {
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl).path(DEFAULT_REST_PREFIX)
        .path(DEFAULT_LOGIN_PATH);
    if(!Strings.isNullOrEmpty(serviceName) && !Strings.isNullOrEmpty(serviceKey)) {
      builder.queryParam("application", serviceName).queryParam("key", serviceKey);
    }
    builder.queryParam("rememberMe", token.isRememberMe());
    return builder.build().toUriString();
  }

  private String getValidateUrl(String ticket) {
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl).path(DEFAULT_REST_PREFIX)
        .path(DEFAULT_VALIDATE_PATH);
    if(!Strings.isNullOrEmpty(serviceName) && !Strings.isNullOrEmpty(serviceKey)) {
      builder.queryParam("application", serviceName).queryParam("key", serviceKey);
    }
    return builder.buildAndExpand(ticket).toUriString();
  }

  private String getSubjectUrl(String ticketId) {
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl).path(DEFAULT_REST_PREFIX)
        .path(DEFAULT_SUBJECT_PATH);
    if(!Strings.isNullOrEmpty(serviceName) && !Strings.isNullOrEmpty(serviceKey)) {
      builder.queryParam("application", serviceName).queryParam("key", serviceKey);
    }
    return builder.buildAndExpand(ticketId).toUriString();
  }

  private String getTicketUrl(String id) {
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl).path(DEFAULT_REST_PREFIX)
        .path(DEFAULT_TICKET_PATH);
    if(!Strings.isNullOrEmpty(serviceName) && !Strings.isNullOrEmpty(serviceKey)) {
      builder.queryParam("application", serviceName).queryParam("key", serviceKey);
    }
    return builder.buildAndExpand(id).toUriString();
  }

  public static class Subject {

    private String username;

    private List<String> groups;

    private List<Map<String, String>> attributes;

    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }

    public List<String> getGroups() {
      return groups;
    }

    public void setGroups(List<String> groups) {
      this.groups = groups;
    }

    public List<Map<String, String>> getAttributes() {
      return attributes;
    }

    public void setAttributes(List<Map<String, String>> attributes) {
      this.attributes = attributes;
    }
  }

}
