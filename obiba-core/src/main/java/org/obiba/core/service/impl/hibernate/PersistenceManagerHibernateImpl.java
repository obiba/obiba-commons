/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.core.service.impl.hibernate;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.obiba.core.service.PagingClause;
import org.obiba.core.service.PersistenceManager;
import org.obiba.core.service.SortingClause;
import org.obiba.core.service.impl.DefaultPersistenceManagerImpl;
import org.obiba.core.validation.exception.ValidationRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * Hibernate implementation of {@link PersistenceManager}.
 *
 */
@Transactional
public class PersistenceManagerHibernateImpl extends DefaultPersistenceManagerImpl {

  private static final Logger log = LoggerFactory.getLogger(PersistenceManagerHibernateImpl.class);

  protected SessionFactory sessionFactory = null;

  private boolean cacheTemplateQueries = false;

  public void setSessionFactory(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  public void setCacheTemplateQueries(boolean cacheTemplateQueries) {
    this.cacheTemplateQueries = cacheTemplateQueries;
  }

  protected Session getSession() {
    return sessionFactory.getCurrentSession();
  }

  @Override
  public int count(Class<?> type) {
    return count(getSession().createCriteria(type));
  }

  @Override
  public int count(Object template) {
    return count(getSession().createCriteria(template.getClass()).add(AssociationExample.create(template)));
  }

  @Override
  public void delete(Object entity) {
    getSession().delete(entity);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T get(Class<T> type, Serializable id) {
    return (T) getSession().get(type, id);
  }

  @Override
  public Serializable getId(Object o) {
    return getSession().getIdentifier(o);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> List<T> list(Class<T> type, PagingClause paging, SortingClause... clauses) {
    return AssociationCriteria.create(type, getSession()).addPagingClause(paging).addSortingClauses(clauses)
        .getCriteria().setCacheable(cacheTemplateQueries).list();
  }

  @Override
  public <T> List<T> list(Class<T> type, SortingClause... clauses) {
    return list(type, null, clauses);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> List<T> match(T template, PagingClause paging, SortingClause... clauses) {
    return mathCriteria(template, paging, clauses).list();
  }

  @Override
  public <T> List<T> match(T template, SortingClause... clauses) {
    return match(template, null, clauses);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T matchOne(T template, SortingClause... clauses) {
    return (T) mathCriteria(template, null, clauses).uniqueResult();
  }

  protected <T> Criteria mathCriteria(T template, PagingClause paging, SortingClause... clauses) {
    return AssociationCriteria.create(template.getClass(), getSession())
        .add("", AssociationCriteria.Operation.match, template).addPagingClause(paging).addSortingClauses(clauses)
        .getCriteria().setCacheable(cacheTemplateQueries);
  }

  @Override
  public <T> T newInstance(Class<T> type) {
    try {
      return type.newInstance();
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public <T> T refresh(T entity) {
    getSession().refresh(entity);
    return entity;
  }

  @Override
  public <T> T save(T entity) throws ValidationRuntimeException {
    validate(entity);
    getSession().save(entity);
    return entity;
  }

  protected int count(Criteria criteria) {
    Object res = criteria.setProjection(Projections.rowCount()).uniqueResult();
    return res != null ? Long.valueOf(res.toString()).intValue() : 0;
  }

}
