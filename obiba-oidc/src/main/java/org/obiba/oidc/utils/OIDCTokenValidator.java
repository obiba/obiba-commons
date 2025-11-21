package org.obiba.oidc.utils;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jwt.JWT;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import com.nimbusds.openid.connect.sdk.validators.IDTokenValidator;
import org.obiba.oidc.OIDCConfiguration;
import org.obiba.oidc.OIDCException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

/**
 * ID Token validator (borrowed from pac4j).
 */
public class OIDCTokenValidator {

  private static final Logger log = LoggerFactory.getLogger(OIDCTokenValidator.class);

  private final List<IDTokenValidator> idTokenValidators;

  public OIDCTokenValidator(final OIDCConfiguration configuration) {

    // check algorithms
    final List<JWSAlgorithm> metadataAlgorithms = configuration.findProviderMetaData().getIDTokenJWSAlgs();
    if (metadataAlgorithms == null || metadataAlgorithms.isEmpty())
      throw new OIDCException("There must at least one JWS algorithm supported on the OpenID Connect provider side");

    List<JWSAlgorithm> jwsAlgorithms = new ArrayList<>();
    final JWSAlgorithm preferredAlgorithm = configuration.hasPreferredJwsAlgorithm() ? JWSAlgorithm.parse(configuration.getPreferredJwsAlgorithm()) : null;
    if (preferredAlgorithm != null && metadataAlgorithms.contains(preferredAlgorithm)) {
      jwsAlgorithms.add(preferredAlgorithm);
    } else {
      jwsAlgorithms = metadataAlgorithms;
      log.warn("Preferred JWS algorithm: {} not available. Using all metadata algorithms: {}",
          preferredAlgorithm, metadataAlgorithms);
    }

    idTokenValidators = new ArrayList<>();
    final ClientID _clientID = new ClientID(configuration.getClientId());
    final Secret _secret = new Secret(configuration.getSecret());

    for (JWSAlgorithm jwsAlgorithm : jwsAlgorithms) {
      if ("none".equals(jwsAlgorithm.getName())) {
        jwsAlgorithm = null;
      }

      // build validator
      final IDTokenValidator idTokenValidator;
      if (jwsAlgorithm == null) {
        idTokenValidator = new IDTokenValidator(getIssuer(configuration), _clientID);
      } else if (configuration.hasSecret() && (JWSAlgorithm.HS256.equals(jwsAlgorithm) ||
          JWSAlgorithm.HS384.equals(jwsAlgorithm) || JWSAlgorithm.HS512.equals(jwsAlgorithm))) {
        idTokenValidator = createHMACTokenValidator(configuration, jwsAlgorithm, _clientID, _secret);
      } else {
        idTokenValidator = createRSATokenValidator(configuration, jwsAlgorithm, _clientID);
      }
      idTokenValidator.setMaxClockSkew(configuration.getMaxClockSkew());

      idTokenValidators.add(idTokenValidator);
    }
  }

  private Issuer getIssuer(final OIDCConfiguration configuration) {
    OIDCProviderMetadata providerMetadata = configuration.findProviderMetaData();
    Issuer issuer = providerMetadata.getIssuer();
    if (issuer == null) {
      throw new OIDCException("No issuer found in provider metadata");
    }
    return issuer;
  }

  protected IDTokenValidator createRSATokenValidator(final OIDCConfiguration configuration,
                                                     final JWSAlgorithm jwsAlgorithm, final ClientID clientID) {
    try {
      return new IDTokenValidator(configuration.findProviderMetaData().getIssuer(), clientID, jwsAlgorithm,
          configuration.findProviderMetaData().getJWKSetURI().toURL(), new DefaultResourceRetriever());
    } catch (final MalformedURLException e) {
      throw new OIDCException(e);
    }
  }

  protected IDTokenValidator createHMACTokenValidator(final OIDCConfiguration configuration, final JWSAlgorithm jwsAlgorithm,
                                                      final ClientID clientID, final Secret secret) {
    return new IDTokenValidator(configuration.findProviderMetaData().getIssuer(), clientID, jwsAlgorithm, secret);
  }

  public IDTokenClaimsSet validate(final JWT idToken, final Nonce expectedNonce)
      throws BadJOSEException, JOSEException {

    BadJOSEException badJOSEException = null;
    JOSEException joseException = null;
    for (final IDTokenValidator idTokenValidator : idTokenValidators) {

      try {
        return idTokenValidator.validate(idToken, expectedNonce);
      } catch (final BadJOSEException e1) {
        badJOSEException = e1;
      } catch (final JOSEException e2) {
        joseException = e2;
      }
    }

    if (badJOSEException != null) {
      throw badJOSEException;
    } else if (joseException != null) {
      throw joseException;
    } else {
      throw new OIDCException("Unable to validate the ID token");
    }
  }
}
