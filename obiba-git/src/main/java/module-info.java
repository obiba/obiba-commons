module obiba.git {
  requires com.google.common;
  requires jakarta.validation;
  requires org.eclipse.jgit;
  requires shiro.core;
  requires spring.context;
  requires spring.core;
  requires obiba.core;
  requires slf4j.api;

  exports org.obiba.git;
  exports org.obiba.git.command;
}