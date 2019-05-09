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

import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import org.obiba.oidc.OIDCConfiguration;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class OIDCAuthenticationRequestFactory {

  private final String callbackURI;

  public OIDCAuthenticationRequestFactory(String callbackURI) {
    this.callbackURI = callbackURI;
  }

  public AuthenticationRequest create(OIDCConfiguration configuration) throws URISyntaxException, IOException, ParseException {
    OIDCProviderMetadata providerMetadata = OIDCAuthenticationHelper.discoverProviderMetaData(configuration);

    // Generate random state string for pairing the response to the request
    State state = new State();
    // Generate nonce
    Nonce nonce = new Nonce();
    // Specify scope
    Scope scope = Scope.parse(configuration.getScope());

    AuthenticationRequest authenticationRequest = new AuthenticationRequest(
        providerMetadata.getAuthorizationEndpointURI(),
        new ResponseType(ResponseType.Value.CODE),
        scope, new ClientID(configuration.getClientId()), new URI(callbackURI), state, nonce);
    return authenticationRequest;
  }

}
