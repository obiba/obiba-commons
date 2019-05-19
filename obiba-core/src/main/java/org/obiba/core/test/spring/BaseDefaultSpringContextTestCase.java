/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.core.test.spring;

import org.hibernate.SessionFactory;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

/**
 * Base class for Spring-enabled unit tests. Simply extend this class in order to enable
 * the test Spring Context for the execution of the test case.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-spring-context.xml" })
@TransactionConfiguration(transactionManager = "transactionManager")
@TestExecutionListeners(
    value = { DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitAwareTestExecutionListener.class })
public abstract class BaseDefaultSpringContextTestCase {

  private SessionFactory sessionFactory;

  @Autowired(required = false)
  public void setSessionFactory(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  /**
   * Flushes (executed pending statements) and clears the Hibernate cache.
   */
  protected void flushCache() {
    if(sessionFactory == null) {
      throw new IllegalStateException("Cannot flush hibernate cache: missing sessionFactory dependency.");
    }
    sessionFactory.getCurrentSession().flush();
    sessionFactory.getCurrentSession().clear();
  }
}
