package org.obiba.core.service;

import org.obiba.core.validation.exception.ValidationRuntimeException;


/**
 * An interface for managing POJO persistence.
 */
public interface PersistenceManager extends EntityQueryService {

  public void delete(Object entity);

  public <T> T newInstance(Class<T> type);

  public <T> T save(T entity) throws ValidationRuntimeException;

}
