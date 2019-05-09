package org.obiba.oidc;

import com.nimbusds.oauth2.sdk.ParseException;
import org.junit.Test;
import org.obiba.oidc.utils.OIDCAuthenticationRequestFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class OIDCTest {

  @Test
  //@Ignore
  public void test() throws IOException, URISyntaxException, ParseException {
    OIDCConfiguration config = new OIDCConfiguration();
    config.setClientId("opal");
    config.setDiscoveryURI("http://localhost:8899/auth/realms/obiba/.well-known/openid-configuration");
    OIDCAuthenticationRequestFactory factory = new OIDCAuthenticationRequestFactory("https://opal-demo.obiba.org/auth/callback");

    URI authReqURI = factory.create(config).toURI();
    System.out.println(authReqURI);
  }

}
