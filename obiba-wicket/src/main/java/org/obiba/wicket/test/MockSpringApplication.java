package org.obiba.wicket.test;

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.util.tester.DummyHomePage;

/**
 * A configurable Wicket {@code WebApplication} implementation that can inject member variables annotated with
 * {@code @SpringBean}.
 * 
 */
public class MockSpringApplication extends WebApplication {

  Class<? extends Page> homePage = DummyHomePage.class;

  @Override
  protected void init() {
    super.init();
    super.addComponentInstantiationListener(new SpringComponentInjector(this));
    getResourceSettings().setThrowExceptionOnMissingResource(false);
  }

  public void setHomePage(Class<? extends Page> homePage) {
    this.homePage = homePage;
  }

  @Override
  public Class<? extends Page> getHomePage() {
    return homePage;
  }

}
