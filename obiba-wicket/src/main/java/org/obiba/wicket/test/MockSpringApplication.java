package org.obiba.wicket.test;

import org.apache.wicket.spring.SpringWebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.util.tester.DummyHomePage;

/**
 * A configurable Wicket {@code WebApplication} implementation that can inject member variables annotated with
 * {@code @SpringBean}.
 * 
 */
public class MockSpringApplication extends SpringWebApplication {

  Class<?> homePage = DummyHomePage.class;

  @Override
  protected void init() {
    super.init();
    super.addComponentInstantiationListener(new SpringComponentInjector(this, internalGetApplicationContext()));
    getResourceSettings().setThrowExceptionOnMissingResource(false);
  }

  public void setHomePage(Class<?> homePage) {
    this.homePage = homePage;
  }

  @Override
  public Class<?> getHomePage() {
    return homePage;
  }

}
