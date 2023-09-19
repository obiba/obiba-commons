module obiba.mongodb {
  requires com.fasterxml.jackson.annotation;
  requires joda.time;
  requires spring.data.commons;

  exports org.obiba.mongodb.domain;
}