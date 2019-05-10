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

import com.google.common.base.Strings;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import org.obiba.oidc.OIDCConfiguration;
import org.obiba.oidc.web.J2EContext;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class OIDCHelper {

  public static OIDCProviderMetadata discoverProviderMetaData(OIDCConfiguration configuration) throws ParseException, IOException {
    URL providerConfigurationURL = new URL(configuration.getDiscoveryURI());
    InputStream stream = providerConfigurationURL.openStream();
    // Read all data from URL
    String providerInfo;
    try (java.util.Scanner s = new java.util.Scanner(stream)) {
      providerInfo = s.useDelimiter("\\A").hasNext() ? s.next() : "";
    }
    return OIDCProviderMetadata.parse(providerInfo);
  }

  /**
   * Extract the provider parameter from the request.
   *
   * @param context
   * @param providerParameter if null, provider name is the last segment in the request path, otherwise, it is the value of the query parameter.
   * @return the provider name or null if not found.
   */
  public static String extractProviderName(J2EContext context, String providerParameter) {
    String provider;
    if (Strings.isNullOrEmpty(providerParameter)) {
      // provider name is last path segment
      String path = context.getPath();
      provider = path.substring(path.lastIndexOf("/") + 1);
    } else {
      // provider is in the query
      provider = context.getRequestParameter("provider");
    }
    return provider;
  }

}
