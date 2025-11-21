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

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import org.obiba.oidc.utils.OIDCHelper;

import java.util.HashMap;
import java.util.Map;

public class OIDCConfiguration {

  private static final int DEFAULT_TIMEOUT = 500;

  private static int DEFAULT_MAX_CLOCK_SKEW = 30;

  private String name;

  // OpenID client identifier
  private String clientId;

  // OpenID secret
  private String secret;

  // discovery URI for fetching OP metadata (http://openid.net/specs/openid-connect-discovery-1_0.html)
  private String discoveryURI;

  // Prompt
  private String prompt;

  private Integer maxAge;

  // Scope
  private String scope = "openid";

  // Nonce
  private boolean useNonce;

  // Map containing user defined parameters
  private Map<String, String> customParams = new HashMap<>();

  private int connectTimeout = DEFAULT_TIMEOUT;

  private int readTimeout = DEFAULT_TIMEOUT;

  private String preferredJwsAlgorithm;

  private int maxClockSkew = DEFAULT_MAX_CLOCK_SKEW;

  private String callbackURL;

  private transient OIDCProviderMetadata oidcProviderMetadata;

  public OIDCConfiguration() {
    this("oidc");
  }

  public OIDCConfiguration(String name) {
    this.name = name;
  }

  public synchronized  OIDCProviderMetadata findProviderMetaData() {
    if (oidcProviderMetadata == null) {
      try {
        // TODO make it expire?
        oidcProviderMetadata = OIDCHelper.discoverProviderMetaData(this);
      } catch (Exception e) {
        throw new OIDCException("Cannot get OIDC provider metadata for " + name, e);
      }
    }
    return oidcProviderMetadata;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCallbackURL() {
    return callbackURL;
  }

  public void setCallbackURL(String callbackURL) {
    this.callbackURL = callbackURL;
  }

  public boolean hasCallbackURL() {
    return !Strings.isNullOrEmpty(callbackURL);
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

  public boolean hasSecret() {
    return !Strings.isNullOrEmpty(secret);
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

  public void setUseNonce(boolean useNonce) {
    this.useNonce = useNonce;
  }

  public boolean isUseNonce() {
    return useNonce;
  }

  public String getPrompt() {
    return prompt;
  }

  public void setPrompt(String prompt) {
    this.prompt = prompt;
  }

  public boolean hasPrompt() {
    return !Strings.isNullOrEmpty(prompt);
  }

  public Integer getMaxAge() {
    return maxAge;
  }

  public void setMaxAge(Integer maxAge) {
    this.maxAge = maxAge;
  }

  public boolean hasMaxAge() {
    return maxAge != null && maxAge >= 0;
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

  public void setCustomParam(String key, String value) {
    getCustomParams().put(key, value);
  }

  public int getConnectTimeout() {
    return connectTimeout;
  }

  public void setConnectTimeout(int connectTimeout) {
    this.connectTimeout = connectTimeout;
  }

  public int getReadTimeout() {
    return readTimeout;
  }

  public void setReadTimeout(int readTimeout) {
    this.readTimeout = readTimeout;
  }

  public String getPreferredJwsAlgorithm() {
    return preferredJwsAlgorithm;
  }

  public void setPreferredJwsAlgorithm(String preferredJwsAlgorithm) {
    this.preferredJwsAlgorithm = preferredJwsAlgorithm;
  }

  public boolean hasPreferredJwsAlgorithm() {
    return !Strings.isNullOrEmpty(preferredJwsAlgorithm);
  }

  public int getMaxClockSkew() {
    return maxClockSkew;
  }

  public void setMaxClockSkew(int maxClockSkew) {
    this.maxClockSkew = maxClockSkew;
  }

}
