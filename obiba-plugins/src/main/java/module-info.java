module obiba.plugins {
  requires com.fasterxml.jackson.annotation;
  requires com.fasterxml.jackson.core;
  requires com.fasterxml.jackson.databind;
  requires com.google.common;
  requires obiba.core;

  exports org.obiba.plugins;
  exports org.obiba.plugins.spi;
}