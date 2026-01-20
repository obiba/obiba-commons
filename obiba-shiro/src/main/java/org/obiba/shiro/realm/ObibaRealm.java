/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.shiro.realm;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.BasicHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authc.credential.AllowAllCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import java.util.Base64;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.obiba.shiro.NoSuchOtpException;
import org.obiba.shiro.authc.TicketAuthenticationToken;
import org.obiba.shiro.authc.UsernamePasswordOtpToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.lang.Nullable;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.net.URLEncoder.encode;

/**
 * A realm for the CAS-like implementation protocol by Obiba.
 */
public class ObibaRealm extends AuthorizingRealm {

  private final static Logger log = LoggerFactory.getLogger(ObibaRealm.class);

  public static final String OBIBA_REALM = "obiba-realm";

  public static final String TICKET_COOKIE_NAME = "obibaid";

  public static final String APPLICATION_AUTH_HEADER = "X-App-Auth";

  public static final String APPLICATION_AUTH_SCHEMA = "Basic";

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

  private GroupsToRolesMapper groupsToRolesMapper;

  public ObibaRealm() {
    super(null, new AllowAllCredentialsMatcher());
    groupsToRolesMapper = new GroupsToRolesMapper() {};
  }

  public void setGroupsToRolesMapper(GroupsToRolesMapper groupsToRolesMapper) {
    this.groupsToRolesMapper = groupsToRolesMapper;
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

  private synchronized AuthenticationInfo doGetUsernameAuthenticationInfo(UsernamePasswordToken token)
      throws AuthenticationException {
    String username = token.getUsername();

    // Null username is invalid
    if(Strings.isNullOrEmpty(username)) {
      throw new AccountException("Empty usernames are not allowed by this realm.");
    }

    try {
      RestTemplate template = newRestTemplate();
      HttpHeaders headers = new HttpHeaders();
      headers.set(APPLICATION_AUTH_HEADER, getApplicationAuth());
      if (token instanceof UsernamePasswordOtpToken otpToken) {
        if (otpToken.hasOtp())
          headers.set("X-Obiba-TOTP", otpToken.getOtp());
      }
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
      headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON.toString());
      String form = "username=" + encode(username, "UTF-8") + "&password=" + encode(new String(token.getPassword()), "UTF-8");
      HttpEntity<String> entity = new HttpEntity<String>(form, headers);

      ResponseEntity<String> response = template.exchange(getLoginUrl(token), HttpMethod.POST, entity, String.class);
      if (response.getStatusCode() == HttpStatus.CREATED) {
        HttpHeaders responseHeaders = response.getHeaders();
        String ticketId = getTicketIdFromHeaders(responseHeaders);
        SecurityUtils.getSubject().getSession().setAttribute(TICKET_COOKIE_NAME, ticketId);
        List<String> principals = Lists.newArrayList(username);
        if (!Strings.isNullOrEmpty(ticketId)) principals.add(ticketId);
        return new SimpleAuthenticationInfo(new SimplePrincipalCollection(principals, getName()), token.getCredentials());
      }

      // not an account in this realm
      log.debug("Invalid credentials. Response status code [{}], response body [{}], credentials used [{}]", response.getStatusCode(), response.getBody(), token);
      return null;
    } catch(HttpClientErrorException e) {
      List<String> wwwAuths = e.getResponseHeaders().get("WWW-Authenticate");
      if (wwwAuths != null && !wwwAuths.isEmpty()) {
        log.debug("Invalid OTP. Response status code [{}], response body [{}], credentials used [{}]", e.getStatusCode(), e.getResponseBodyAsString(), token);
        String qrImage = null;
        if (!Strings.isNullOrEmpty(e.getResponseBodyAsString())) {
          try {
            JsonObject respObj = JsonParser.parseString(e.getResponseBodyAsString()).getAsJsonObject();
            if (respObj.has("image"))
              qrImage = respObj.get("image").getAsString();
          } catch (Exception ej) {
            // ignore
          }
        }
        throw new NoSuchOtpException(wwwAuths.get(0), qrImage);
      }
      if (log.isDebugEnabled())
        log.error("Connection failure with identification server", e);
      else
        log.error("Connection failure with identification server: [%s]".formatted(e.getMessage()));
      return null;
    } catch(ResourceAccessException e) {
      if (log.isDebugEnabled())
        log.error("Connection failure with identification server", e);
      else
        log.error("Connection failure with identification server: [%s]".formatted(e.getMessage()));
      return null;
    } catch(Exception e) {
      if (log.isDebugEnabled())
        log.error("Authentication failure", e);
      else
        log.error("Authentication failure: [%s]".formatted(e.getMessage()));
      throw new AuthenticationException("Failed authenticating on " + baseUrl, e);
    }
  }

  private synchronized AuthenticationInfo doGetTicketAuthenticationInfo(TicketAuthenticationToken token)
      throws AuthenticationException {
    // Null ticket id is invalid
    if(Strings.isNullOrEmpty(token.getTicketId())) {
      throw new AccountException("Empty tickets are not allowed by this realm.");
    }

    try {
      RestTemplate template = newRestTemplate();
      HttpHeaders headers = new HttpHeaders();
      headers.set(APPLICATION_AUTH_HEADER, getApplicationAuth());
      HttpEntity<String> entity = new HttpEntity<String>(null, headers);

      ResponseEntity<String> response = template.exchange(getValidateUrl(token.getTicketId()), HttpMethod.GET, entity, String.class);

      if(response.getStatusCode() == HttpStatus.OK) {
        HttpHeaders responseHeaders = response.getHeaders();
        String ticketId = getTicketIdFromHeaders(responseHeaders);
        SecurityUtils.getSubject().getSession().setAttribute(TICKET_COOKIE_NAME, ticketId);
        List<String> principals = Lists.newArrayList(response.getBody());
        if(!Strings.isNullOrEmpty(ticketId)) principals.add(ticketId);
        return new SimpleAuthenticationInfo(new SimplePrincipalCollection(principals, getName()),token.getCredentials());
      }

      // not an account in this realm
      log.info("Invalid ticket. Response status code [{}], response body [{}], ticket used [{}]", response.getStatusCode(), response.getBody(), token);
      return null;
    } catch(HttpClientErrorException|ResourceAccessException e) {
      log.error("Impossible to contact identification server: [%s]".formatted(e.getMessage()), e);
      return null;
    } catch(Exception e) {
      throw new AuthenticationException("Failed authenticating on " + baseUrl, e);
    }
  }

  private String getTicketIdFromHeaders(HttpHeaders responseHeaders) {
    String ticketId = null;

    for(String cookieValue : responseHeaders.get(SET_COOKIE_HEADER)) {
      if(cookieValue.startsWith(TICKET_COOKIE_NAME + "=")) {
        // set in the subject's session the cookie that will allow to perform the single sign-on
        SecurityUtils.getSubject().getSession().setAttribute(SET_COOKIE_HEADER, cookieValue);
        // keep ticket reference for logout
        ticketId = cookieValue.split(";")[0].substring(TICKET_COOKIE_NAME.length() + 1);
      }
    }

    return ticketId;
  }

  @Override
  protected synchronized AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
    Collection<?> thisPrincipals = principals.fromRealm(getName());

    if(thisPrincipals != null && !thisPrincipals.isEmpty()) {
      Set<String> groups = Sets.newHashSet(getName());
      try {
        JWTClaimsSet webToken = getWebTokenFromPrincipals(thisPrincipals);
        if(webToken != null) {
          TicketContextUser user = new ObjectMapper()
              .convertValue(webToken.getJSONObjectClaim("context").get("user"),
                  TicketContextUser.class);
          if (user.getGroups() != null)
            groups.addAll(user.getGroups());
        } else { //backward compatibility. web token not found in principals.
          RestTemplate template = newRestTemplate();
          HttpHeaders headers = new HttpHeaders();
          headers.set(APPLICATION_AUTH_HEADER, getApplicationAuth());
          HttpEntity<String> entity = new HttpEntity<String>(null, headers);
          ResponseEntity<Subject> response = template
              .exchange(getSubjectUrl(getTicketFromSession()), HttpMethod.GET, entity, Subject.class);

          if(response.getStatusCode().equals(HttpStatus.OK) && response.getBody().groups != null) {
            if (response.getBody().groups != null)
              groups.addAll(response.getBody().groups);
          }
        }
      } catch(HttpClientErrorException e) {
        return new SimpleAuthorizationInfo();
      } catch(Exception e) {
        throw new AuthenticationException("Failed authorizing on " + baseUrl, e);
      }
      return new SimpleAuthorizationInfo(groupsToRolesMapper.toRoles(groups));
    }
    return new SimpleAuthorizationInfo();
  }

  private JWTClaimsSet getWebTokenFromPrincipals(Collection<?> principals) {
    for(Object principal : principals) {
      try {
        String[] webTokenParts = principal.toString().split("\\.");
        if(webTokenParts.length > 1) {
          String webToken = principal.toString();
          // note: do not validate because it was obtained in a trusted way (and only agate can validate)
          return JWTParser.parse(webToken).getJWTClaimsSet();
        }
      } catch(Exception e) {
        log.error("Error while parsing JWT", e);
      }
    }

    return null;
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
        HttpHeaders headers = new HttpHeaders();
        headers.set(APPLICATION_AUTH_HEADER, getApplicationAuth());
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);

        template.exchange(getTicketUrl(ticketId), HttpMethod.DELETE, entity, String.class);
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
   * @param serviceName
   */
  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  /**
   * Service key issuing credentials requests.
   *
   * @param serviceKey
   */
  public void setServiceKey(String serviceKey) {
    this.serviceKey = serviceKey;
  }

  @Override
  public String getName() {
    return OBIBA_REALM;
  }

  private RestTemplate newRestTemplate() {
    log.debug("Connecting to Agate: {}", baseUrl);
    if (baseUrl.toLowerCase().startsWith("https://")) {
      if(httpRequestFactory == null) {
        httpRequestFactory = new HttpComponentsClientHttpRequestFactory(createHttpClient());
      }
      return new RestTemplate(httpRequestFactory);
    } else {
      return new RestTemplate();
    }
  }

  private CloseableHttpClient createHttpClient() {
    HttpClientBuilder builder = HttpClients.custom();
    try {
      SSLConnectionSocketFactory sslsf = getSocketFactory();
      Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create()
          .register("https", sslsf)
          .register("http", new PlainConnectionSocketFactory())
          .build();
      BasicHttpClientConnectionManager connectionManager =
          new BasicHttpClientConnectionManager(socketFactoryRegistry);
      builder.setConnectionManager(connectionManager);
    } catch(NoSuchAlgorithmException | KeyManagementException e) {
      throw new RuntimeException(e);
    }

    return builder.build();
  }

  /**
   * Do not check anything from the remote host (Agate server is trusted).
   * @return
   * @throws NoSuchAlgorithmException
   * @throws KeyManagementException
   */
  private SSLConnectionSocketFactory getSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
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

    return new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
  }

  private String getLoginUrl(UsernamePasswordToken token) {
    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl).path(DEFAULT_REST_PREFIX)
        .path(DEFAULT_LOGIN_PATH);
    builder.queryParam("rememberMe", token.isRememberMe());
    return builder.build().toUriString();
  }

  private String getValidateUrl(String ticket) {
    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl).path(DEFAULT_REST_PREFIX)
        .path(DEFAULT_VALIDATE_PATH);
    return builder.buildAndExpand(ticket).toUriString();
  }

  private String getSubjectUrl(String ticketId) {
    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl).path(DEFAULT_REST_PREFIX)
        .path(DEFAULT_SUBJECT_PATH);
    return builder.buildAndExpand(ticketId).toUriString();
  }

  private String getTicketUrl(String id) {
    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl).path(DEFAULT_REST_PREFIX)
        .path(DEFAULT_TICKET_PATH);
    return builder.buildAndExpand(id).toUriString();
  }

  private String getApplicationAuth() {
    String token = serviceName + ":" + serviceKey;
    return APPLICATION_AUTH_SCHEMA + " " + Base64.getEncoder().encodeToString(token.getBytes());
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

  private static class TicketContextUser {
    private List<String> groups;
    private String name;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    private String locale;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getFirstName() {
      return firstName;
    }

    public void setFirstName(String firstName) {
      this.firstName = firstName;
    }

    public String getLastName() {
      return lastName;
    }

    public void setLastName(String lastName) {
      this.lastName = lastName;
    }

    public String getLocale() {
      return locale;
    }

    public void setLocale(String locale) {
      this.locale = locale;
    }

    public List<String> getGroups() {
      return groups;
    }

    public void setGroups(List<String> groups) {
      this.groups = groups;
    }
  }
}
