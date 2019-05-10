package org.obiba.oidc;

import com.nimbusds.oauth2.sdk.ParseException;
import org.junit.Ignore;
import org.junit.Test;
import org.obiba.oidc.utils.OIDCAuthenticationRequestFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class OIDCTest {

  @Test
  @Ignore
  public void test() throws IOException, URISyntaxException, ParseException {
    KeycloakOIDCConfiguration config = new KeycloakOIDCConfiguration();
    config.setClientId("opal");
    config.setBaseUri("http://localhost:8899/auth");
    config.setRealm("obiba");
    OIDCAuthenticationRequestFactory factory = new OIDCAuthenticationRequestFactory("https://opal-demo.obiba.org/auth/callback");

    URI authReqURI = factory.create(config).toURI();
    System.out.println(authReqURI);
  }

}
