/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.ssl;

import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Set;

import javax.annotation.Nullable;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedKeyManager;

import org.obiba.security.KeyStoreManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of {@code X509ExtendedKeyManager} on {@link org.obiba.security.KeyStoreManager}. This implementation will list all available key
 * pairs in the system keystore and select the first one that matches the requested algorithm.
 */
public class X509ExtendedKeyManagerImpl extends X509ExtendedKeyManager {

  private static final Logger log = LoggerFactory.getLogger(X509ExtendedKeyManagerImpl.class);

  /**
   * The key alias to use first when looking up keypairs for serving HTTPs.
   */
  public static final String HTTPS_ALIAS = "https";

  private final KeyStoreManager keyStoreManager;

  public X509ExtendedKeyManagerImpl(KeyStoreManager keyStoreManager) {
    if(keyStoreManager == null) throw new IllegalArgumentException("KeyStoreManager cannot be null");
    this.keyStoreManager = keyStoreManager;
  }

  @Override
  public String chooseClientAlias(String[] keyTypes, Principal[] issuers, @Nullable Socket socket) {
    log.debug("chooseClientAlias({}, {}, socket)", keyTypes, issuers);
    for(String keyType : keyTypes) {
      if(isKeyType(HTTPS_ALIAS, keyType)) {
        return HTTPS_ALIAS;
      }
      String alias = chooseServerAlias(keyType, issuers, socket);
      if(alias != null) {
        return alias;
      }
    }
    return null;
  }

  @Override
  public String chooseServerAlias(String keyType, Principal[] issuers, @Nullable Socket socket) {
    log.debug("Requested keyType: '{}'", keyType);
    if(isKeyType(HTTPS_ALIAS, keyType)) {
      log.debug("Selecting key '{}'", HTTPS_ALIAS);
      return HTTPS_ALIAS;
    }
    for(String alias : keyStoreManager.listKeyPairs()) {
      KeyPair pair = keyStoreManager.getKeyPair(alias);
      if(pair.getPrivate().getAlgorithm().equals(keyType)) {
        log.debug("Selecting key '{}'", alias);
        return alias;
      }
    }
    log.debug("No appropriate key pair found for SSL.");
    return null;
  }

  @Override
  public X509Certificate[] getCertificateChain(String alias) {
    log.debug("getCertificateChain({})", alias);
    try {
      Certificate[] certs = keyStoreManager.getKeyStore().getCertificateChain(alias);
      // Convert Certificate[] to X509Certificate[]
      return Arrays.copyOf(certs, certs.length, X509Certificate[].class);
    } catch(KeyStoreException e) {
      throw new RuntimeException(e);
    }
  }

  @Nullable
  @Override
  public String[] getClientAliases(String keyType, Principal[] issuers) {
    log.debug("getClientAliases({}, {})", keyType, issuers);
    return null;
  }

  @Override
  public PrivateKey getPrivateKey(String alias) {
    log.debug("getPrivateKey({})", alias);
    return keyStoreManager.getKeyPair(alias).getPrivate();
  }

  @Override
  public String[] getServerAliases(String keyType, Principal[] issuers) {
    log.debug("getServerAliases({}, {})", keyType, issuers);
    Set<String> keyPairs = keyStoreManager.listKeyPairs();
    return keyPairs.toArray(new String[keyPairs.size()]);
  }

  @Override
  public String chooseEngineClientAlias(String[] keyTypes, Principal[] issuers, SSLEngine engine) {
    log.debug("chooseEngineClientAlias({}, {})", keyTypes, issuers);
    return chooseClientAlias(keyTypes, issuers, null);
  }

  @Override
  public String chooseEngineServerAlias(String keyType, Principal[] issuers, SSLEngine engine) {
    log.debug("chooseEngineServerAlias({}, {})", keyType, Arrays.toString(issuers));
    return chooseServerAlias(keyType, issuers, null);
  }

  /**
   * Returns true if the key {@code alias} exists and its algorithm is {@code keyType}
   *
   * @param alias
   * @param keyType
   * @return
   */
  private boolean isKeyType(String alias, String keyType) {
    return keyStoreManager.hasKeyPair(alias) && keyStoreManager.getKeyPair(alias).getPrivate().getAlgorithm().equals(keyType);
  }

}
