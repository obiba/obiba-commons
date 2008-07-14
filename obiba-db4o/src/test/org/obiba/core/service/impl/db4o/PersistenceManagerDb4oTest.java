package org.obiba.core.service.impl.db4o;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obiba.core.domain.AbstractEntity;
import org.obiba.core.service.impl.db4o.PersistenceManagerDb4oImpl;
import org.obiba.core.service.impl.db4o.testModel.A;
import org.obiba.core.service.impl.db4o.testModel.B;
import org.obiba.core.service.impl.db4o.testModel.C;
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
@ContextConfiguration(locations={"db4o-context.xml"})
@TransactionConfiguration(transactionManager="transactionManager")
@TestExecutionListeners(value={DependencyInjectionTestExecutionListener.class,DirtiesContextTestExecutionListener.class,TransactionalTestExecutionListener.class})
@Transactional
public class PersistenceManagerDb4oTest {

  private PersistenceManagerDb4oImpl persistenceManager;

  @Autowired(required=true)
  public void setPersistenceManager(PersistenceManagerDb4oImpl persistenceManager) {
    this.persistenceManager = persistenceManager;
  }

  @Test
  public void testCountNonExistantEntityReturnsZero() {
    Assert.assertEquals(0, persistenceManager.count(Yo.class));
  }

  @Test
  public void testInsertEntity() {
    Yo entity = new Yo();
    entity.setValue(105);
    this.persistenceManager.save(entity);
    Assert.assertNotNull(entity.getId());
  }

  @Test
  public void testActivation() {
    A a = new A();
    a.setValue(1);
    B b = new B();
    b.setParent(a);
    b.setValue(2);
    a.setBees(Collections.singletonList(b));
    C c = new C();
    c.setParent(b);
    c.setValue(3);
    b.setCees(Collections.singletonList(c));
    persistenceManager.save(a);

    // Make sure Db4o removes its reference to a 
    // Otherwise when we lookup for a, Db4o will return the same reference...
    persistenceManager.purge(a);

    A template = new A();
    template.setValue(1);
    A entity = persistenceManager.matchOne(template);

    Assert.assertNotNull(entity);
    // Make sure we have different objects
    Assert.assertFalse(entity == a);
    Assert.assertEquals(new Integer(1), entity.getValue());
//    Assert.assertFalse(b == entity.getBees().get(0));
    Assert.assertEquals(new Integer(2), entity.getBees().get(0).getValue());
  //  Assert.assertFalse(c == entity.getBees().get(0).getCees().get(0));
    Assert.assertEquals(new Integer(3), entity.getBees().get(0).getCees().get(0).getValue());
  }

  private class Yo extends AbstractEntity {

    private static final long serialVersionUID = 4566181431595363198L;

    private Integer value;

    public Integer getValue() {
      return value;
    }

    public void setValue(Integer value) {
      this.value = value;
    }
  }

}
