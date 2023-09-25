package org.obiba.ssl;

import org.obiba.security.KeyStoreManager;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;

/**
 *
 */
public class X509TrustManagerImpl implements X509TrustManager {

  private X509TrustManager defaultTrustManager;

  private final KeyStoreManager keyStoreManager;

  private final boolean checkClient;

  private final boolean checkServer;

  public X509TrustManagerImpl(KeyStoreManager keyStoreManager, boolean checkClient, boolean checkServer) throws NoSuchAlgorithmException, KeyStoreException {
    this.keyStoreManager = keyStoreManager;
    this.checkClient = checkClient;
    this.checkServer = checkServer;
    TrustManagerFactory tmFact = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    tmFact.init((KeyStore) null); // default trust managers
    for (TrustManager tm : tmFact.getTrustManagers()) {
      if (tm instanceof X509TrustManager manager) {
        defaultTrustManager = manager;
      }
    }
  }

  @Override
  public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    try {
      defaultTrustManager.checkClientTrusted(chain, authType);
    } catch (CertificateException excep) {
      if (checkClient) checkTrusted(chain);
    }
  }

  @Override
  public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    try {
      defaultTrustManager.checkServerTrusted(chain, authType);
    } catch (CertificateException excep) {
      if (checkServer) checkTrusted(chain);
    }
  }

  @Override
  public X509Certificate[] getAcceptedIssuers() {
    return defaultTrustManager.getAcceptedIssuers();
  }

  /**
   * Trust if at least one of the certificate in the chain is known.
   *
   * @param chain
   */
  private void checkTrusted(X509Certificate[] chain) throws CertificateException {
    Collection<Certificate> knownCerts = keyStoreManager.getCertificates().values();
    for(X509Certificate x509Cert : chain) {
      for (Certificate cert : knownCerts) {
        try {
          x509Cert.verify(cert.getPublicKey());
          // If verify succeeds, it doesn't throw an Exception
          return;
        } catch(GeneralSecurityException e) {
          // Ignore
        }
      }
    }
    throw new CertificateException("Not a trusted certificate");
  }
}
