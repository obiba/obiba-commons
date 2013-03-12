package org.obiba.core.service.impl.hibernate;

import java.io.Serializable;

import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obiba.core.service.impl.hibernate.testModel.AnEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "hibernateEntity-context.xml" })
@TransactionConfiguration(transactionManager = "transactionManager")
@TestExecutionListeners(
    value = { DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class })
@Transactional
public class HibernateEntityTest {

  private SessionFactory sessionFactory;

  @Autowired(required = true)
  public void setSessionFactory(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Test
  public void testAbstractEntityMapsIdToLong() {
    AnEntity a = new AnEntity();
    a.setName("My Entity");
    Serializable id = sessionFactory.getCurrentSession().save(a);
    Assert.assertTrue(id instanceof Long);
  }

}
