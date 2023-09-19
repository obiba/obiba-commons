module obiba.jersey.shiro {
  requires java.annotation;
  requires java.ws.rs;
  requires shiro.core;
  requires slf4j.api;

  exports org.obiba.jersey.shiro.authz.annotation;
}