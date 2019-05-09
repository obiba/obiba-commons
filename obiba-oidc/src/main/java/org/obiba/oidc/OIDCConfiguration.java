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

import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.Map;

public class OIDCConfiguration {

  private String name;

  // OpenID client identifier
  private String clientId;

  // OpenID secret
  private String secret;

  // discovery URI for fetching OP metadata (http://openid.net/specs/openid-connect-discovery-1_0.html)
  private String discoveryURI;

  // Scope
  private String scope = "openid";

  // Map containing user defined parameters
  private Map<String, String> customParams = new HashMap<>();

  public OIDCConfiguration() {
    this("oidc");
  }

  public OIDCConfiguration(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getSecret() {
    return secret;
  }

  public void setSecret(String secret) {
    this.secret = secret;
  }

  public String getDiscoveryURI() {
    return discoveryURI;
  }

  public void setDiscoveryURI(String discoveryURI) {
    this.discoveryURI = discoveryURI;
  }

  public String getScope() {
    return scope;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }

  public Map<String, String> getCustomParams() {
    return customParams == null ? customParams = Maps.newHashMap() : customParams;
  }

  public String getCustomParam(String name) {
    return getCustomParams().get(name);
  }

  public void setCustomParams(Map<String, String> customParams) {
    this.customParams = customParams;
  }
}
