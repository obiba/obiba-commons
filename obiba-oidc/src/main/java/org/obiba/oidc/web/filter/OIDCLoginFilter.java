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
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import org.obiba.oidc.OIDCConfiguration;
import org.obiba.oidc.OIDCConfigurationProvider;
import org.obiba.oidc.OIDCStateManager;
import org.obiba.oidc.utils.OIDCAuthenticationRequestFactory;
import org.obiba.oidc.utils.OIDCHelper;
import org.obiba.oidc.web.J2EContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Performs the login against the identified ID provider.
 */
public class OIDCLoginFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(OIDCLoginFilter.class);

  private OIDCConfigurationProvider oidcConfigurationProvider;

  private String callbackURL = "http://localhost:8080/auth/callback/";

  private String providerParameter;

  private OIDCStateManager oidcStateManager;

  /**
   * Get the ID provider configuration to now how to submit the login request.
   *
   * @param oidcConfigurationProvider
   */
  public void setOIDCConfigurationProvider(OIDCConfigurationProvider oidcConfigurationProvider) {
    this.oidcConfigurationProvider = oidcConfigurationProvider;
  }

  /**
   * Set the State manager.
   *
   * @param oidcStateManager
   */
  public void setOIDCStateManager(OIDCStateManager oidcStateManager) {
    this.oidcStateManager = oidcStateManager;
  }

  /**
   * Set the client application callback URL that will be submitted to the ID provider (with the provider name appended).
   *
   * @param callbackURL
   */
  public void setCallbackURL(String callbackURL) {
    this.callbackURL = callbackURL;
  }

  /**
   * When defined, the provider parameter will be used both for extracting the ID provider name from the login request
   * and also for building the callback URL that will be submitted to the provider.
   *
   * @param providerParameter
   */
  public void setProviderParameter(String providerParameter) {
    this.providerParameter = providerParameter;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    J2EContext context = new J2EContext(request, response);

    String provider = OIDCHelper.extractProviderName(context, providerParameter);
    if (Strings.isNullOrEmpty(provider)) {
      log.error("No ID provider could be identified.");
    } else {
      try {
        OIDCConfiguration config = oidcConfigurationProvider.getConfiguration(provider);
        OIDCAuthenticationRequestFactory factory = new OIDCAuthenticationRequestFactory(makeCallbackURL(provider));
        AuthenticationRequest authRequest = factory.create(config);
        if (oidcStateManager != null)
          oidcStateManager.saveState(context.getRemoteAddr() + "_" + context.getRequest().getSession().getId(), authRequest.getState());
        response.sendRedirect(authRequest.toURI().toString());
      } catch (Exception e) {
        log.error("OIDC login request to '{}' failed.", provider, e);
      }
    }

    filterChain.doFilter(request, response);
  }

  protected String makeCallbackURL(String provider) {
    if (Strings.isNullOrEmpty(providerParameter)) {
      return callbackURL + (callbackURL.endsWith("/") ? "" : "/") + provider;
    } else {
      return callbackURL + "?" + providerParameter + "=" + provider;
    }
  }
}
