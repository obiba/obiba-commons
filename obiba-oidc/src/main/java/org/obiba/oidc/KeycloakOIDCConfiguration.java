/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.oidc;

public class KeycloakOIDCConfiguration extends OIDCConfiguration {

  /**
   * Keycloak auth realm
   */
  private String realm;
  /**
   * Keycloak server base uri
   */
  private String baseUri;

  @Override
  public String getDiscoveryURI() {
    return baseUri + "/realms/" + realm + "/.well-known/openid-configuration";
  }

  public String getRealm() {
    return realm;
  }

  public void setRealm(String realm) {
    this.realm = realm;
  }

  public String getBaseUri() {
    return baseUri;
  }

  public void setBaseUri(String baseUri) {
    this.baseUri = baseUri;
  }

}
