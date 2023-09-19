module obiba.oidc {
  requires java.scripting;
  requires org.openjdk.nashorn;
  requires com.google.common;
  requires jakarta.servlet;
  requires json.smart;
  requires nimbus.jose.jwt;
  requires oauth2.oidc.sdk;
  requires org.json;
  requires shiro.core;
  requires spring.web;
  requires slf4j.api;

  exports org.obiba.oidc;
  exports org.obiba.oidc.shiro.authc;
  exports org.obiba.oidc.shiro.realm;
  exports org.obiba.oidc.utils;
  exports org.obiba.oidc.web;
  exports org.obiba.oidc.web.filter;
}