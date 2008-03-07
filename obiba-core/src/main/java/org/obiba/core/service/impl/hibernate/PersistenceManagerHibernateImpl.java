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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;


/**
 * Hibernate implementation of {@link PersistenceManager}.
 * 
 * @param <T>
 */
@Transactional
public class PersistenceManagerHibernateImpl implements PersistenceManager {

  private final Logger log = LoggerFactory.getLogger(PersistenceManagerHibernateImpl.class);

  protected SessionFactory sessionFactory = null;
  
  public void setSessionFactory(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }
  
  protected Session getSession() {
    return sessionFactory.getCurrentSession();
  }

  public int count(Class<?> type) {
    return count(getSession().createCriteria(type));
  }

  public int count(Object template) {
    return count(getSession().createCriteria(template.getClass()).add(AssociationExample.create(template)));
  }

  public void delete(Object entity) {
    getSession().delete(entity);
  }

  @SuppressWarnings("unchecked")
  public <T> T get(Class<T> type, Serializable id) {
    return (T)getSession().get(type, id);
  }

  public Serializable getId(Object o) {
    return getSession().getIdentifier(o);
  }

  @SuppressWarnings("unchecked")
  public <T> List<T> list(Class<T> type, PagingClause paging, SortingClause... clauses) {
    return AssociationCriteria.create(type, getSession()).addPagingClause(paging).addSortingClauses(clauses).getCriteria().list();
  }

  @SuppressWarnings("unchecked")
  public <T> List<T> list(Class<T> type, SortingClause... clauses) {
    return this.list(type, null, clauses);
  }

  @SuppressWarnings("unchecked")
  public <T> List<T> match(T template, PagingClause paging, SortingClause... clauses) {
    return mathCriteria(template, paging, clauses).list();
  }

  public <T> List<T> match(T template, SortingClause... clauses) {
    return this.match(template, null, clauses);
  }

  @SuppressWarnings("unchecked")
  public <T> T matchOne(T template, SortingClause... clauses) {
    return (T)mathCriteria(template, null, clauses).uniqueResult();
  }
  
  protected <T> Criteria mathCriteria(T template, PagingClause paging, SortingClause... clauses) {
    return AssociationCriteria.create(template.getClass(), getSession()).add("", AssociationCriteria.Operation.match, template).addPagingClause(paging).addSortingClauses(clauses).getCriteria();
  }

  public <T> T newInstance(Class<T> type) {
    try {
      return type.newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public <T> T refresh(T entity) {
    getSession().refresh(entity);
    return entity;
  }

  public <T> T save(T entity) {
    getSession().save(entity);
    return entity;
  }

  protected int count(Criteria criteria) {
    return (Integer)criteria.setProjection(Projections.rowCount()).uniqueResult();
  }
}
