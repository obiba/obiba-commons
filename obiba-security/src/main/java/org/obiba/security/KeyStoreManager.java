/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.security;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStore.Entry;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStore.TrustedCertificateEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.validation.constraints.NotNull;

import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.obiba.crypt.CacheablePasswordCallback;
import org.obiba.crypt.CachingCallbackHandler;
import org.obiba.crypt.KeyPairNotFoundException;
import org.obiba.crypt.KeyProviderException;
import org.obiba.crypt.KeyProviderSecurityException;
import org.obiba.crypt.ObibaCryptRuntimeException;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;

public class KeyStoreManager {

  public static final String PASSWORD_FOR = "Password for";

  public enum KeyType {
    KEY_PAIR, CERTIFICATE
  }

  private final String name;

  private final KeyStore store;

  private CallbackHandler callbackHandler;

  public KeyStoreManager(String name, KeyStore store) {
    this.name = name;
    this.store = store;
  }

  public Set<String> listAliases() {
    try {
      return ImmutableSet.copyOf(Iterators.forEnumeration(store.aliases()));
    } catch(KeyStoreException e) {
      throw new RuntimeException(e);
    }
  }

  public Entry getEntry(String alias) {
    try {
      if(store.isKeyEntry(alias)) {
        CacheablePasswordCallback passwordCallback = createPasswordCallback("Password for '" + alias + "':  ");
        return store.getEntry(alias, new PasswordProtection(getKeyPassword(passwordCallback)));
      }
      if(store.isCertificateEntry(alias)) {
        return store.getEntry(alias, null);
      }
      throw new UnsupportedOperationException("Unsupported key type for alias " + alias);
    } catch(KeyStoreException | IOException | UnsupportedCallbackException | UnrecoverableEntryException | NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  private CacheablePasswordCallback createPasswordCallback(String prompt) {
    return CacheablePasswordCallback.Builder.newCallback().key(name).prompt(prompt).build();
  }

  public Set<String> listKeyPairs() {
    return ImmutableSet.copyOf(Iterables.filter(listAliases(), new Predicate<String>() {

      @Override
      public boolean apply(String input) {
        try {
          return store.isKeyEntry(input) && store.entryInstanceOf(input, PrivateKeyEntry.class);
        } catch(KeyStoreException e) {
          throw new RuntimeException(e);
        }
      }
    }));
  }

  public Set<String> listCertificates() {
    return ImmutableSet.copyOf(Iterables.filter(listAliases(), new Predicate<String>() {

      @Override
      public boolean apply(String input) {
        try {
          return store.isCertificateEntry(input);
        } catch(KeyStoreException e) {
          throw new RuntimeException(e);
        }
      }
    }));
  }

  public boolean hasKeyPair(String alias) {
    return listKeyPairs().contains(alias);
  }

  public KeyPair getKeyPair(String alias) {
    try {
      return findKeyPairForPrivateKey(alias);
    } catch(KeyPairNotFoundException ex) {
      throw ex;
    } catch(UnrecoverableKeyException ex) {
      if(callbackHandler instanceof CachingCallbackHandler) {
        ((CachingCallbackHandler) callbackHandler).clearPasswordCache(name);
      }
      throw new KeyProviderSecurityException("Wrong key password");
    } catch(Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public KeyPair getKeyPair(PublicKey publicKey) {
    try {
      return findKeyPairForPublicKey(publicKey, store.aliases());
    } catch(KeyStoreException ex) {
      throw new RuntimeException(ex);
    }
  }

  public X509Certificate importCertificate(String alias, InputStream pem) {
    X509Certificate cert = getCertificate(pem);
    try {
      store.setCertificateEntry(alias, cert);
    } catch(KeyStoreException e) {
      throw new ObibaCryptRuntimeException(e);
    }
    return cert;
  }

  public Map<String, Certificate> getCertificates() {
    Map<String, Certificate> map = Maps.newHashMap();
    for(String alias : listAliases()) {
      Entry keyEntry = getEntry(alias);
      if(keyEntry instanceof TrustedCertificateEntry) {
        map.put(alias, ((TrustedCertificateEntry) keyEntry).getTrustedCertificate());
      }
    }
    return map;
  }

  public void setCallbackHandler(CallbackHandler callbackHandler) {
    this.callbackHandler = callbackHandler;
  }

  public String getName() {
    return name;
  }

  public KeyStore getKeyStore() {
    return store;
  }

  private char[] getKeyPassword(CacheablePasswordCallback passwordCallback)
      throws UnsupportedCallbackException, IOException {
    callbackHandler.handle(new CacheablePasswordCallback[] { passwordCallback });
    return passwordCallback.getPassword();
  }

  private KeyPair findKeyPairForPrivateKey(String alias)
      throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, UnsupportedCallbackException,
      IOException {

    Key key = store.getKey(alias, getKeyPassword(createPasswordCallback("Password for '" + alias + "':  ")));
    if(key == null) {
      throw new KeyPairNotFoundException("KeyPair not found for specified alias (" + alias + ")");
    }

    if(key instanceof PrivateKey) {
      // Get certificate of public key
      Certificate cert = store.getCertificate(alias);

      // Get public key
      PublicKey publicKey = cert.getPublicKey();

      // Return a key pair
      return new KeyPair(publicKey, (PrivateKey) key);
    }
    throw new KeyPairNotFoundException("KeyPair not found for specified alias (" + alias + ")");
  }

  private KeyPair findKeyPairForPublicKey(Key publicKey, Enumeration<String> aliases) {
    KeyPair keyPair = null;

    while(aliases.hasMoreElements()) {
      String alias = aliases.nextElement();
      KeyPair currentKeyPair = getKeyPair(alias);

      if(Arrays.equals(currentKeyPair.getPublic().getEncoded(), publicKey.getEncoded())) {
        keyPair = currentKeyPair;
        break;
      }
    }

    if(keyPair == null) {
      throw new KeyPairNotFoundException("KeyPair not found for specified public key");
    }
    return keyPair;
  }

  public static X509Certificate makeCertificate(PrivateKey issuerPrivateKey, PublicKey subjectPublicKey,
      String certificateInfo, String signatureAlgorithm)
      throws SignatureException, InvalidKeyException, CertificateEncodingException, NoSuchAlgorithmException {
    X509V3CertificateGenerator certificateGenerator = new X509V3CertificateGenerator();
    X509Name issuerDN = new X509Name(certificateInfo);
    X509Name subjectDN = new X509Name(certificateInfo);
    int daysTillExpiry = 30 * 365;

    Calendar expiry = Calendar.getInstance();
    expiry.add(Calendar.DAY_OF_YEAR, daysTillExpiry);

    certificateGenerator.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
    certificateGenerator.setIssuerDN(issuerDN);
    certificateGenerator.setSubjectDN(subjectDN);
    certificateGenerator.setPublicKey(subjectPublicKey);
    certificateGenerator.setNotBefore(new Date());
    certificateGenerator.setNotAfter(expiry.getTime());
    certificateGenerator.setSignatureAlgorithm(signatureAlgorithm);
    return certificateGenerator.generate(issuerPrivateKey);
  }

  public void createOrUpdateKey(String alias, String algorithm, int size, String certificateInfo) {
    try {
      KeyPair keyPair = generateKeyPair(algorithm, size);
      X509Certificate cert = makeCertificate(algorithm, certificateInfo, keyPair);
      CacheablePasswordCallback passwordCallback = createPasswordCallback(getPasswordFor(name));
      store.setKeyEntry(alias, keyPair.getPrivate(), getKeyPassword(passwordCallback), new X509Certificate[] { cert });
    } catch(GeneralSecurityException e) {
      throw new ObibaCryptRuntimeException(e);
    } catch(IOException | UnsupportedCallbackException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Deletes the key associated with the provided alias.
   *
   * @param alias key to delete
   */
  public void deleteKey(String alias) {
    try {
      store.deleteEntry(alias);
    } catch(KeyStoreException e) {
      throw new KeyProviderException(e);
    }
  }

  /**
   * Returns true if the provided alias exists.
   *
   * @param alias check if this alias exists in the KeyStore.
   * @return true if the alias exists
   */
  public boolean aliasExists(String alias) {
    try {
      return store.containsAlias(alias);
    } catch(KeyStoreException e) {
      throw new KeyProviderException(e);
    }
  }

  public KeyType getKeyType(String alias) {
    if(listKeyPairs().contains(alias)) {
      return KeyType.KEY_PAIR;
    }
    if(listCertificates().contains(alias)) {
      return KeyType.CERTIFICATE;
    }
    throw new IllegalArgumentException("unknown alias '" + alias + "'or key type");
  }

  public static void loadBouncyCastle() {
    if(Security.getProvider("BC") == null) Security.addProvider(new BouncyCastleProvider());
  }

  /**
   * Import a private key and it's associated certificate into the keystore at the given alias.
   *
   * @param alias name of the key
   * @param privateKey private key in the PEM format
   * @param certificate certificate in the PEM format
   */
  public void importKey(String alias, InputStream privateKey, InputStream certificate) {
    storeKeyEntry(alias, getPrivateKey(privateKey), getCertificate(certificate));
  }

  private void storeKeyEntry(String alias, Key key, X509Certificate cert) {
    CacheablePasswordCallback passwordCallback = createPasswordCallback(getPasswordFor(alias));
    try {
      store.setKeyEntry(alias, key, getKeyPassword(passwordCallback), new X509Certificate[] { cert });
    } catch(KeyStoreException | IOException | UnsupportedCallbackException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Import a private key into the keystore and generate an associated certificate at the given alias.
   *
   * @param alias name of the key
   * @param privateKey private key in the PEM format
   * @param certificateInfo Certificate attributes as a String (e.g. CN=Administrator, OU=Bioinformatics, O=GQ,
   * L=Montreal, ST=Quebec, C=CA)
   */
  public void importKey(String alias, InputStream privateKey, String certificateInfo) {
    makeAndStoreKeyEntry(alias, getKeyPair(privateKey), certificateInfo);
  }

  private void makeAndStoreKeyEntry(String alias, KeyPair keyPair, String certificateInfo) {
    X509Certificate cert;
    try {
      cert = makeCertificate(keyPair.getPrivate(), keyPair.getPublic(), certificateInfo,
          chooseSignatureAlgorithm(keyPair.getPrivate().getAlgorithm()));
      CacheablePasswordCallback passwordCallback = createPasswordCallback(getPasswordFor(alias));
      store.setKeyEntry(alias, keyPair.getPrivate(), getKeyPassword(passwordCallback), new X509Certificate[] { cert });
    } catch(GeneralSecurityException | IOException | UnsupportedCallbackException e) {
      throw new RuntimeException(e);
    }
  }

  private X509Certificate makeCertificate(String algorithm, String certificateInfo, KeyPair keyPair)
      throws SignatureException, InvalidKeyException, CertificateEncodingException, NoSuchAlgorithmException {
    return makeCertificate(keyPair.getPrivate(), keyPair.getPublic(), certificateInfo,
        chooseSignatureAlgorithm(algorithm));
  }

  private KeyPair generateKeyPair(String algorithm, int size) throws NoSuchAlgorithmException {
    KeyPairGenerator keyPairGenerator;
    keyPairGenerator = KeyPairGenerator.getInstance(algorithm);
    keyPairGenerator.initialize(size);
    return keyPairGenerator.generateKeyPair();
  }

  private String chooseSignatureAlgorithm(String keyAlgorithm) {
    // TODO add more algorithms here.
    return "DSA".equals(keyAlgorithm) ? "SHA1withDSA" : "SHA1WithRSA";
  }

  protected KeyPair getKeyPair(InputStream privateKey) {
    try(PEMReader pemReader = getPEMReader(privateKey)) {
      Object object = getPemObject(pemReader);
      if(object instanceof KeyPair) {
        return (KeyPair) object;
      }
      throw new RuntimeException("Unexpected type [" + object + "]. Expected KeyPair.");
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected Key getPrivateKey(InputStream privateKey) {
    try(PEMReader pemReader = getPEMReader(privateKey)) {
      return toPrivateKey(getPemObject(pemReader));
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("ChainOfInstanceofChecks")
  private Key toPrivateKey(Object pemObject) {
    if(pemObject instanceof KeyPair) {
      return ((KeyPair) pemObject).getPrivate();
    }
    if(pemObject instanceof Key) {
      return (Key) pemObject;
    }
    throw new RuntimeException("Unexpected type [" + pemObject + "]. Expected KeyPair or Key.");
  }

  protected X509Certificate getCertificate(InputStream certificate) {
    try(PEMReader pemReader = getPEMReader(certificate)) {
      Object object = getPemObject(pemReader);
      if(object instanceof X509Certificate) {
        return (X509Certificate) object;
      }
      throw new RuntimeException("Unexpected type [" + object + "]. Expected X509Certificate.");
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  @NotNull
  private PEMReader getPEMReader(InputStream certificate) {
    return new PEMReader(new InputStreamReader(certificate), new PasswordFinder() {
      @Override
      public char[] getPassword() {
        return System.console().readPassword("%s:  ", "Password for imported certificate");
      }
    });
  }

  @NotNull
  private Object getPemObject(PEMReader pemReader) throws IOException {
    Object object = pemReader.readObject();
    if(object == null) throw new RuntimeException("No PEM information.");
    return object;
  }

  /**
   * Returns "Password for 'name':  ".
   */
  private String getPasswordFor(String target) {
    return PASSWORD_FOR + " '" + target + "':  ";
  }

  @SuppressWarnings({ "StaticMethodOnlyUsedInOneClass", "ParameterHidesMemberVariable" })
  public static class Builder {

    protected String name;

    protected CallbackHandler callbackHandler;

    public static Builder newStore() {
      return new Builder();
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder passwordPrompt(CallbackHandler callbackHandler) {
      this.callbackHandler = callbackHandler;
      return this;
    }

    private char[] getKeyPassword(CacheablePasswordCallback passwordCallback)
        throws UnsupportedCallbackException, IOException {
      callbackHandler.handle(new CacheablePasswordCallback[] { passwordCallback });
      return passwordCallback.getPassword();
    }

    public KeyStoreManager build() {
      if (name == null || name.isEmpty()) throw new IllegalArgumentException("name must not be null or empty");
      if (callbackHandler == null) throw new IllegalArgumentException("callbackHandler must not be null");

      loadBouncyCastle();

      CacheablePasswordCallback passwordCallback = CacheablePasswordCallback.Builder.newCallback().key(name)
          .prompt("Enter '" + name + "' keystore password:  ")
          .confirmation("Re-enter '" + name + "' keystore password:  ").build();

      KeyStore keyStore = createEmptyKeyStore(passwordCallback);

      return createKeyStoreManager(keyStore);
    }

    protected KeyStore createEmptyKeyStore(CacheablePasswordCallback passwordCallback) {
      KeyStore keyStore = null;
      try {
        keyStore = KeyStore.getInstance("JCEKS");
        keyStore.load(null, getKeyPassword(passwordCallback));
      } catch(KeyStoreException e) {
        clearPasswordCache(callbackHandler, name);
        throw new KeyProviderSecurityException("Wrong keystore password or keystore was tampered with");
      } catch(GeneralSecurityException | UnsupportedCallbackException e) {
        throw new RuntimeException(e);
      } catch(IOException ex) {
        clearPasswordCache(callbackHandler, name);
        translateAndRethrowKeyStoreIOException(ex);
      }
      return keyStore;
    }

    private static void clearPasswordCache(CallbackHandler callbackHandler, String alias) {
      if(callbackHandler instanceof CachingCallbackHandler) {
        ((CachingCallbackHandler) callbackHandler).clearPasswordCache(alias);
      }
    }

    private static void translateAndRethrowKeyStoreIOException(IOException ex) {
      if(ex.getCause() != null && ex.getCause() instanceof UnrecoverableKeyException) {
        throw new KeyProviderSecurityException("Wrong keystore password");
      }
      throw new RuntimeException(ex);
    }

    protected KeyStoreManager createKeyStoreManager(KeyStore keyStore) {
      KeyStoreManager keyStoreManager = new KeyStoreManager(name, keyStore);
      keyStoreManager.setCallbackHandler(callbackHandler);
      return keyStoreManager;
    }
  }

}
