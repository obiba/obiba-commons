package org.obiba.core.service;


/**
 * An interface for managing POJO persistence.
 */
public interface PersistenceManager extends EntityQueryService {

  public void delete(Object entity);

  public <T> T newInstance(Class<T> type);

  public <T> T save(T entity);

}
