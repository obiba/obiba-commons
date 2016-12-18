/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.core.service.impl.hibernate;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.junit.Assert;
import org.junit.Test;
import org.obiba.core.service.SortingClause;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.core.service.impl.hibernate.testModel.A;
import org.obiba.core.service.impl.hibernate.testModel.B;
import org.obiba.core.service.impl.hibernate.testModel.C;
import org.obiba.core.test.spring.BaseDefaultSpringContextTestCase;
import org.obiba.core.test.spring.Dataset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@SuppressWarnings("unchecked")
@Transactional
@Dataset
public class AssociationCriteriaTest extends BaseDefaultSpringContextTestCase {

  private SessionFactory factory;

  @Override
  @Autowired(required = true)
  public void setSessionFactory(SessionFactory factory) {
    this.factory = factory;
  }

  @Test
  public void testMatch() {
    C template = new C();
    template.setValue(201);
    AssociationCriteria ac = AssociationCriteria.create(A.class, factory.getCurrentSession())
        .add("bees.cees", Operation.match, template);
    A result = (A) ac.getCriteria().uniqueResult();
    Assert.assertNotNull(result);
  }

  @Test
  public void testMatchMultiple() {
    A template = new A();
    template.setValue(2);
    AssociationCriteria ac = AssociationCriteria.create(C.class, factory.getCurrentSession())
        .add("parent.parent", Operation.match, template);
    List<C> results = ac.getCriteria().list();
    Assert.assertNotNull(results);
    Assert.assertEquals(3, results.size());
  }

  @Test
  public void testMatchMultipleOrderByMiddleEntity() {
    A template = new A();
    template.setValue(2);
    AssociationCriteria ac = AssociationCriteria.create(C.class, factory.getCurrentSession())
        .add("parent.parent", Operation.match, template).addSortingClauses(SortingClause.create("parent.value", false));

    List<C> results = ac.getCriteria().list();
    Assert.assertNotNull(results);
    Assert.assertEquals(3, results.size());

    C first = results.get(0);
    C second = results.get(1);
    C third = results.get(2);
    Assert.assertTrue(first.getParent().getValue() >= second.getParent().getValue());
    Assert.assertTrue(second.getParent().getValue() >= third.getParent().getValue());
  }

  @Test
  public void testMatchAndOrderByUnreferencedEntity() {
    A template = new A();
    template.setValue(2);
    AssociationCriteria ac = AssociationCriteria.create(B.class, factory.getCurrentSession())
        .add("parent", Operation.match, template).addSortingClauses(SortingClause.create("cees.value", false));
    List<B> results = ac.getCriteria().list();
    Assert.assertNotNull(results);
    Assert.assertEquals(2, results.size());
  }

  @Test
  public void testEq() {
    AssociationCriteria ac = AssociationCriteria.create(B.class, factory.getCurrentSession())
        .add("value", Operation.eq, 102);
    B result = (B) ac.getCriteria().uniqueResult();
    Assert.assertNotNull(result);
    Assert.assertEquals(new Integer(102), result.getValue());
  }

  @Test
  public void testOr() {
    AssociationCriteria ac = AssociationCriteria.create(A.class, factory.getCurrentSession())
        .add("bees", Operation.or, Restrictions.eq("value", 102), Restrictions.eq("value", 101));
    List<A> results = ac.getCriteria().list();
    Assert.assertNotNull(results);
    Assert.assertEquals(2, results.size());
  }

  @Test
  public void testIn() {
    AssociationCriteria ac = AssociationCriteria.create(A.class, factory.getCurrentSession())
        .add("bees.value", Operation.in, 101, 102);
    List<A> results = ac.getCriteria().list();
    Assert.assertNotNull(results);
    Assert.assertEquals(2, results.size());
  }

  @Test
  public void testGt() {
    AssociationCriteria ac = AssociationCriteria.create(A.class, factory.getCurrentSession())
        .add("bees.value", Operation.gt, 101);
    List<A> results = ac.getCriteria().list();
    Assert.assertNotNull(results);
    Assert.assertEquals(1, results.size());
  }

  @Test
  public void testGe() {
    AssociationCriteria ac = AssociationCriteria.create(A.class, factory.getCurrentSession())
        .add("bees.value", Operation.ge, 101);
    List<A> results = ac.getCriteria().list();
    Assert.assertNotNull(results);
    Assert.assertEquals(2, results.size());
  }

  @Test
  public void testLt() {
    AssociationCriteria ac = AssociationCriteria.create(A.class, factory.getCurrentSession())
        .add("bees.value", Operation.lt, 102);
    List<A> results = ac.getCriteria().list();
    Assert.assertNotNull(results);
    Assert.assertEquals(1, results.size());
  }

  @Test
  public void testLe() {
    AssociationCriteria ac = AssociationCriteria.create(A.class, factory.getCurrentSession())
        .add("bees.value", Operation.le, 102);
    List<A> results = ac.getCriteria().list();
    Assert.assertNotNull(results);
    Assert.assertEquals(2, results.size());
  }

  @Test
  public void testNullSortingClause() {
    AssociationCriteria ac = AssociationCriteria.create(A.class, factory.getCurrentSession()).addSortingClauses();
    List<A> results = ac.getCriteria().list();
    Assert.assertNotNull(results);
  }

  @Test
  public void testNoAddNoSort() {
    AssociationCriteria ac = AssociationCriteria.create(A.class, factory.getCurrentSession());
    List<A> results = ac.getCriteria().list();
    Assert.assertNotNull(results);
  }
}
