package org.obiba.core.service.impl.db4o;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.obiba.core.service.impl.db4o.testModel.A;
import org.obiba.core.service.impl.db4o.testModel.B;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;


import com.db4o.ObjectContainer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"db4o-context.xml", "db4o-services.xml"})
@TransactionConfiguration(transactionManager="transactionManager")
@TestExecutionListeners(value={DependencyInjectionTestExecutionListener.class,DirtiesContextTestExecutionListener.class,TransactionalTestExecutionListener.class})
public class Db4oEntityServiceTest {

//  @Autowired(required=true)
  private ObjectContainer objectContainer;
  
  @Test
  public void testSave() {
    A a = new A();
    a.setValue(1);
    
    List<B> bees = new ArrayList<B>(10);
    B b = new B();
    b.setValue(100);
    bees.add(b);
    b = new B();
    b.setValue(101);
    bees.add(b);
    a.setBees(bees);

//    service.save(a);
//    objectContainer.ext().purge(a);

    A template = new A();
    template.setValue(1);

//    A result = service.matchOne(a);

//    Assert.assertFalse(a == result);
//    Assert.assertEquals(a.getBees(), result.getBees());
//    Assert.assertTrue(a.getBees().get(0).getParent() == null);
  }

}
