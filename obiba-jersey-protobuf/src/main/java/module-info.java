module obiba.jersey.protobuf {
  requires java.ws.rs;
  requires com.google.common;
  requires findbugs.annotations;
  requires protobuf.java;
  requires protobuf.java.util;
  requires obiba.web.model;
  requires slf4j.api;

  exports org.obiba.jersey.protobuf;
  exports org.obiba.jersey.exceptionmapper;
}