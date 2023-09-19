module obiba.security {
  requires com.google.common;
  requires jakarta.validation;
  requires slf4j.api;
  requires org.bouncycastle.provider;

  exports org.obiba.security;
  exports  org.obiba.crypt;
  exports  org.obiba.crypt.x509;
  exports  org.obiba.ssl;
}