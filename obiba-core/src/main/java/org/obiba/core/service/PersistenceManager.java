package org.obiba.core.service;

import org.obiba.core.validation.exception.ValidationRuntimeException;

/**
 * An interface for managing POJO persistence.
 */
public interface PersistenceManager extends EntityQueryService {

  void delete(Object entity);

  <T> T newInstance(Class<T> type);

  <T> T save(T entity) throws ValidationRuntimeException;

}
