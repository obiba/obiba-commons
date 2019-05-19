/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.wicket.test;

import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.ISpringContextLocator;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.util.tester.DummyHomePage;
import org.obiba.wicket.application.ISpringWebApplication;
import org.springframework.context.ApplicationContext;

/**
 * A configurable Wicket {@code WebApplication} implementation that can inject member variables annotated with
 * {@code @SpringBean}.
 */
public class MockSpringApplication extends WebApplication implements ISpringWebApplication {

  private ApplicationContext context;

  Class<? extends Page> homePage = DummyHomePage.class;

  /**
   * Singleton instance of spring application context locator
   */
  private final static ISpringContextLocator contextLocator = new ISpringContextLocator() {

    private static final long serialVersionUID = 1L;

    @Override
    public ApplicationContext getSpringContext() {
      Application app = Application.get();
      return ((MockSpringApplication) app).getApplicationContext();
    }
  };

  @Override
  protected void init() {
    super.init();
    addComponentInstantiationListener(new SpringComponentInjector(this, context, true));
    getResourceSettings().setThrowExceptionOnMissingResource(false);
  }

  public void setHomePage(Class<? extends Page> homePage) {
    this.homePage = homePage;
  }

  @Override
  public Class<? extends Page> getHomePage() {
    return homePage;
  }

  public ApplicationContext getApplicationContext() {
    return context;
  }

  public void setApplicationContext(ApplicationContext context) {
    this.context = context;
  }

  @Override
  public ISpringContextLocator getSpringContextLocator() {
    return contextLocator;
  }
}
