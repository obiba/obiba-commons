package org.obiba.ssl;

import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Do not check certificate validity.
 */
public class AnyTrustManager implements X509TrustManager {

  @Override
  public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {

  }

  @Override
  public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {

  }

  @Override
  public X509Certificate[] getAcceptedIssuers() {
    return null;
  }
}
