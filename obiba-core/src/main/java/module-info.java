module obiba.core {
  requires java.desktop;
  requires java.sql;
  requires commons.beanutils;
  requires dbunit;
  requires findbugs.annotations;
  requires json.path;
  requires junit;
  requires org.mockito;
  requires org.json;
  requires spring.aop;
  requires spring.beans;
  requires spring.context;
  requires spring.core;
  requires spring.jdbc;
  requires spring.test;
  requires spring.tx;
  requires winzipaes;
  requires xstream;
  requires slf4j.api;

  exports org.obiba.core.service;
  exports org.obiba.core.service.impl;
  exports org.obiba.core.spring.xstream;
  exports org.obiba.core.test.spring;
  exports org.obiba.core.translator;
  exports org.obiba.core.util;
  exports org.obiba.core.validation.validator;
  exports org.obiba.core.validation.exception;
  exports org.obiba.core.validation.interceptor;
  exports org.obiba.runtime;
  exports org.obiba.runtime.jdbc;
  exports org.obiba.runtime.upgrade;
  exports org.obiba.runtime.upgrade.support;
  exports org.obiba.runtime.upgrade.support.jdbc;
}