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


import org.obiba.oidc.utils.OIDCAuthenticationRequestFactory;
import org.obiba.oidc.OIDCConfiguration;
import org.obiba.oidc.OIDCConfigurationProvider;
import org.obiba.oidc.web.J2EContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class OIDCSecurityFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(OIDCSecurityFilter.class);

  private OIDCConfigurationProvider oidcConfigurationProvider;

  private String callbackURL = "http://localhost:8080/auth/callback/";

  public void setOIDCConfigurationProvider(OIDCConfigurationProvider oidcConfigurationProvider) {
    this.oidcConfigurationProvider = oidcConfigurationProvider;
  }

  public void setCallbackURL(String callbackURL) {
    this.callbackURL = callbackURL;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    J2EContext context = new J2EContext(request, response);

    String path = context.getPath();
    String provider = context.getRequestParameter("provider");

    OIDCConfiguration config = oidcConfigurationProvider.getConfiguration("kc-test");
    OIDCAuthenticationRequestFactory factory = new OIDCAuthenticationRequestFactory(callbackURL + "kc-test");
    try {
      response.sendRedirect(factory.create(config).toURI().toString());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    filterChain.doFilter(request, response);
  }
}
