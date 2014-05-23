package org.obiba.shiro.realm;

import java.util.List;

import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.AllowAllCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

public class RestRealm extends AuthorizingRealm {

  public static final String REST_REALM = "obiba-rest-realm";

  public static final String DEFAULT_REST_PREFIX = "/ws";

  public static final String DEFAULT_SESSION_CREATE_PATH = "/auth/sessions";

  private String baseUrl = "http://localhost:8888";

  public RestRealm() {
    this(null);
  }

  public RestRealm(CacheManager cacheManager) {
    super(cacheManager, new AllowAllCredentialsMatcher());
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
    UsernamePasswordToken upToken = (UsernamePasswordToken) token;
    String username = upToken.getUsername();

    // Null username is invalid
    if(username == null) {
      throw new AccountException("Null usernames are not allowed by this realm.");
    }

    try {
      RestTemplate template = new RestTemplate();
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
      String form = "username=" + username + "&password=" + new String(upToken.getPassword());
      HttpEntity<String> entity = new HttpEntity<String>(form, headers);
      ResponseEntity<String> response = template.exchange(getCreateSessionUrl(), HttpMethod.POST, entity, String.class);

      if(response.getStatusCode().equals(HttpStatus.CREATED)) {
        HttpHeaders responseHeaders = response.getHeaders();
        List<String> cookies = responseHeaders.get("Set-Cookie");
        String cookieValue = cookies.get(0);
        return new SimpleAuthenticationInfo(username, "Set-Cookie:" + cookieValue, getName());
      }

      // not an account in this realm
      return null;
    } catch (HttpClientErrorException e) {
      return null;
    } catch (Exception e) {
      throw new AuthenticationException("Failed authenticating on " + baseUrl, e);
    }
  }

  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
    return null;
  }

  public void setBaseUrl(String baseUrl) {
    if (baseUrl.endsWith("/")) {
      this.baseUrl = baseUrl.substring(0, baseUrl.length()-1);
    } else {
      this.baseUrl = baseUrl;
    }
  }

  @Override
  public String getName() {
    return REST_REALM;
  }

  private String getCreateSessionUrl() {
    return baseUrl + DEFAULT_REST_PREFIX + DEFAULT_SESSION_CREATE_PATH;
  }

}
