/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.oidc.web.filter;


import com.google.common.base.Strings;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.oauth2.sdk.*;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.*;
import com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.obiba.oidc.*;
import org.obiba.oidc.utils.OIDCHelper;
import org.obiba.oidc.utils.OIDCTokenValidator;
import org.obiba.oidc.web.J2EContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class OIDCCallbackFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(OIDCCallbackFilter.class);

  private OIDCConfigurationProvider oidcConfigurationProvider;

  private String defaultRedirectURL = "http://localhost:8080";

  private String callbackURL = "http://localhost:8080/auth/callback/";

  private String providerParameter;

  private OIDCSessionManager oidcSessionManager;

  /**
   * Access to the provider configurations.
   *
   * @param oidcConfigurationProvider
   */
  public void setOIDCConfigurationProvider(OIDCConfigurationProvider oidcConfigurationProvider) {
    this.oidcConfigurationProvider = oidcConfigurationProvider;
  }

  /**
   * Set the manager of the session.
   *
   * @param oidcSessionManager
   */
  public void setOIDCSessionManager(OIDCSessionManager oidcSessionManager) {
    this.oidcSessionManager = oidcSessionManager;
  }

  /**
   * Set where to redirect after callback has been processed.
   *
   * @param defaultRedirectURL
   */
  public void setDefaultRedirectURL(String defaultRedirectURL) {
    this.defaultRedirectURL = defaultRedirectURL;
  }

  /**
   * Returns the default redirect URL
   *
   * @return defaultRedirectURL
   */
  protected String getDefaultRedirectURL() {
    return defaultRedirectURL;
  }

  /**
   * Set the provider parameter in the query string. If not specified, the provider name is the last segment in the request path.
   *
   * @param providerParameter
   */
  public void setProviderParameter(String providerParameter) {
    this.providerParameter = providerParameter;
  }

  /**
   * Returns the parameter in the query string.
   *
   * @return providerParameter
   */
  protected String getProviderParameter() {
    return providerParameter;
  }

  /**
   * Set the client application callback URL.
   *
   * @param callbackURL
   */
  public void setCallbackURL(String callbackURL) {
    this.callbackURL = callbackURL;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    J2EContext context = new J2EContext(request, response);

    String provider = OIDCHelper.extractProviderName(context, providerParameter);
    if (Strings.isNullOrEmpty(provider)) {
      log.error("No ID provider could be identified.");
    } else {
      doOIDCDance(context, provider);
    }

    onRedirect(oidcSessionManager.getSession(context.getClientId()), context, provider);
    filterChain.doFilter(request, response);
  }

  /**
   * Called on any error (session, connection, validation), before throwing a {@link OIDCException}.
   *
   * @param session
   * @param error
   */
  protected void onAuthenticationError(OIDCSession session, String error, HttpServletResponse response) {
    log.error(error);
    if (session != null)
      session.setCallbackError(error);
  }

  /**
   * Called when all validation steps have been successful.
   *
   * @param session
   * @param credentials
   */
  protected void onAuthenticationSuccess(OIDCSession session, OIDCCredentials credentials, HttpServletResponse response) {

  }

  /**
   * Called to perform redirect. Can be overridden to change behavior when there was an error or depending on the
   * original request parameters.
   *
   * @param session
   * @param context
   * @param provider
   */
  protected void onRedirect(OIDCSession session, J2EContext context, String provider) throws IOException {
    context.getResponse().sendRedirect(defaultRedirectURL);
  }

  private void doOIDCDance(J2EContext context, String provider) {
    if (!oidcSessionManager.hasSession(context.getClientId())) {
      String error = "Cannot find OIDC session. Is it expired?";
      onAuthenticationError(null, error, context.getResponse());
      throw new OIDCSessionException(error, null);
    }

    try {
      OIDCConfiguration config = oidcConfigurationProvider.getConfiguration(provider);
      if (config == null)
        throw new OIDCException("No OIDC configuration could be found: " + provider);
      AuthenticationSuccessResponse authResponse = extractAuthenticationResponse(context, config);
      if (authResponse.getAuthorizationCode() != null) {
        OIDCCredentials credentials = validate(context, config, authResponse);
        extractUserInfo(context, config, credentials);
        onAuthenticationSuccess(oidcSessionManager.getSession(context.getClientId()), credentials, context.getResponse());
      }
    } catch (Exception e) {
      log.error("OIDC callback request from '{}' failed.", provider, e);
    }
  }

  private AuthenticationSuccessResponse extractAuthenticationResponse(J2EContext context, OIDCConfiguration config) {
    Map<String, String> parameters = retrieveParameters(context);
    String computedCallbackUrl = context.getFullRequestURL();

    AuthenticationResponse response;
    try {
      response = AuthenticationResponseParser.parse(new URI(computedCallbackUrl), parameters);
    } catch (final URISyntaxException | ParseException e) {
      throw new OIDCException(e);
    }

    if (response instanceof AuthenticationErrorResponse) {
      String error = ((AuthenticationErrorResponse) response).getErrorObject().toJSONObject().toString();
      OIDCSession session = oidcSessionManager.getSession(context.getClientId());
      onAuthenticationError(session, error, context.getResponse());
      throw new OIDCSessionException("Authentication response error: " + error, session);
    }

    log.debug("Authentication response successful");
    AuthenticationSuccessResponse successResponse = (AuthenticationSuccessResponse) response;
    final State state = successResponse.getState();
    if (state == null) {
      throw new OIDCException("Missing state parameter");
    } else if (oidcSessionManager != null && !oidcSessionManager.checkState(context.getClientId(), state)) {
      throw new OIDCException("Not valid or expired state: " + state + " for " + context.getRemoteAddr());
    }

    return successResponse;
  }

  private OIDCCredentials validate(J2EContext context, OIDCConfiguration config, AuthenticationSuccessResponse authResponse) {
    AuthorizationCode code = authResponse.getAuthorizationCode();
    ClientAuthentication clientAuthentication = new ClientSecretBasic(new ClientID(config.getClientId()), new Secret(config.getSecret()));

    try {
      // Token request
      String cbURL = config.hasCallbackURL() ? config.getCallbackURL() : callbackURL;
      final TokenRequest request = new TokenRequest(config.findProviderMetaData().getTokenEndpointURI(),
          clientAuthentication, new AuthorizationCodeGrant(code, new URI(cbURL + config.getName())));
      HTTPRequest tokenHttpRequest = request.toHTTPRequest();
      tokenHttpRequest.setConnectTimeout(config.getConnectTimeout());
      tokenHttpRequest.setReadTimeout(config.getReadTimeout());

      final HTTPResponse httpResponse = tokenHttpRequest.send();
      log.debug("Token response: status={}, content={}", httpResponse.getStatusCode(), httpResponse.getContent());

      final TokenResponse response = OIDCTokenResponseParser.parse(httpResponse);
      if (response instanceof TokenErrorResponse) {
        String error = ((TokenErrorResponse) response).getErrorObject().toJSONObject().toString();
        OIDCSession session = oidcSessionManager.getSession(context.getClientId());
        onAuthenticationError(session, error, context.getResponse());
        throw new OIDCSessionException("Bad token response, error=" + error, session);
      }

      log.debug("Token response successful");
      final OIDCTokenResponse tokenSuccessResponse = (OIDCTokenResponse) response;
      final OIDCTokens oidcTokens = tokenSuccessResponse.getOIDCTokens();

      if (config.isUseNonce()) {
        OIDCTokenValidator validator = new OIDCTokenValidator(config);
        OIDCSession session = oidcSessionManager.getSession(context.getClientId());
        IDTokenClaimsSet claimsSet = validator.validate(oidcTokens.getIDToken(), session.getNonce());
        if (claimsSet == null) {
          onAuthenticationError(session,"ID token cannot be validated", context.getResponse());
          throw new OIDCSessionException("ID token cannot be validated", session);
        }
      }

      // save tokens in credentials
      OIDCCredentials credentials = new OIDCCredentials();
      credentials.setAuthorizationCode(authResponse.getAuthorizationCode());
      credentials.setAccessToken(oidcTokens.getAccessToken());
      credentials.setRefreshToken(oidcTokens.getRefreshToken());
      credentials.setIdToken(oidcTokens.getIDToken());
      return credentials;
    } catch (Exception e) {
      throw new OIDCException(e);
    }
  }

  private void extractUserInfo(J2EContext context, OIDCConfiguration config, OIDCCredentials credentials) {
    final AccessToken accessToken = credentials.getAccessToken();
    try {
      URI userInfoEndpointURI = config.findProviderMetaData().getUserInfoEndpointURI();
      if (userInfoEndpointURI != null && accessToken != null) {
        final UserInfoRequest userInfoRequest = new UserInfoRequest(userInfoEndpointURI, (BearerAccessToken) accessToken);
        final HTTPRequest userInfoHttpRequest = userInfoRequest.toHTTPRequest();
        userInfoHttpRequest.setConnectTimeout(config.getConnectTimeout());
        userInfoHttpRequest.setReadTimeout(config.getReadTimeout());
        final HTTPResponse httpResponse = userInfoHttpRequest.send();
        log.debug("Token response: status={}, content={}", httpResponse.getStatusCode(),
            httpResponse.getContent());

        final UserInfoResponse userInfoResponse = UserInfoResponse.parse(httpResponse);
        if (userInfoResponse instanceof UserInfoErrorResponse) {
          log.error("Bad User Info response ({}), error={}", httpResponse.getStatusMessage(), ((UserInfoErrorResponse) userInfoResponse).getErrorObject().toJSONObject());
        } else {
          final UserInfoSuccessResponse userInfoSuccessResponse = (UserInfoSuccessResponse) userInfoResponse;
          final JWTClaimsSet userInfoClaimsSet;
          if (userInfoSuccessResponse.getUserInfo() != null) {
            userInfoClaimsSet = userInfoSuccessResponse.getUserInfo().toJWTClaimsSet();
          } else {
            userInfoClaimsSet = userInfoSuccessResponse.getUserInfoJWT().getJWTClaimsSet();
          }
          credentials.setUserInfo(userInfoClaimsSet.getClaims());
        }
      }
    } catch (Exception e) {
      throw new OIDCException(e);
    }
  }

  private Map<String, String> retrieveParameters(final J2EContext context) {
    final Map<String, String[]> requestParameters = context.getRequestParameters();
    Map<String, String> map = new HashMap<>();
    for (final Map.Entry<String, String[]> entry : requestParameters.entrySet()) {
      map.put(entry.getKey(), entry.getValue()[0]);
    }
    return map;
  }
}
