/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.oidc.utils;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.Prompt;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import org.obiba.oidc.OIDCConfiguration;
import org.obiba.oidc.OIDCException;

import java.net.URI;
import java.net.URISyntaxException;

public class OIDCAuthenticationRequestFactory {

  private final String callbackURI;

  private final String clientId;

  public OIDCAuthenticationRequestFactory(String callbackURI, String clientId) {
    this.callbackURI = callbackURI;
    this.clientId = clientId;
  }

  public AuthenticationRequest create(OIDCConfiguration configuration) {
    // Generate random state string for pairing the response to the request
    State state = Strings.isNullOrEmpty(clientId) ? new State() : new State(clientId);
    // Generate nonce
    Nonce nonce = configuration.isUseNonce() ? new Nonce() : null;
    // Specify scope
    Scope scope = Scope.parse(configuration.getScope());

    AuthenticationRequest authenticationRequest = null;
    try {
      AuthenticationRequest.Builder builder =
          new AuthenticationRequest.Builder(
              new ResponseType(ResponseType.Value.CODE),
              scope,
              new ClientID(configuration.getClientId()),
              new URI(callbackURI))
              .state(state)
              .endpointURI(getAuthorizationEndpointURI(configuration));
      if (nonce != null) builder.nonce(nonce);
      if (configuration.hasPrompt()) {
        String[] promptValues = Splitter.on(" ")
            .omitEmptyStrings().trimResults()
            .splitToStream(configuration.getPrompt()).toArray(String[]::new);
        if (promptValues.length > 0) builder.prompt(new Prompt(promptValues));
      }
      if (configuration.hasMaxAge()) builder.maxAge(configuration.getMaxAge());
      authenticationRequest = builder.build();
    } catch (URISyntaxException e) {
      throw new OIDCException(e);
    }
    return authenticationRequest;
  }

  private URI getAuthorizationEndpointURI(OIDCConfiguration configuration) {
    OIDCProviderMetadata providerMetadata = configuration.findProviderMetaData();
    URI authorizationEndpointURI = providerMetadata.getAuthorizationEndpointURI();
    if (authorizationEndpointURI == null) {
      throw new OIDCException("No authorization endpoint URI found in provider metadata");
    }
    return authorizationEndpointURI;
  }

}
