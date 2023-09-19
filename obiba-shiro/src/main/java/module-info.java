module obiba.shiro {
  requires java.naming;
  requires com.fasterxml.jackson.annotation;
  requires com.fasterxml.jackson.databind;
  requires com.google.common;
  requires jjwt;
  requires org.apache.httpcomponents.client5.httpclient5;
  requires org.apache.httpcomponents.core5.httpcore5;
  requires shiro.core;
  requires spring.context;
  requires spring.core;
  requires spring.web;
  requires slf4j.api;

  exports org.obiba.shiro;
  exports org.obiba.shiro.authc;
  exports org.obiba.shiro.crypto;
  exports org.obiba.shiro.realm;
}