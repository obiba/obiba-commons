package org.obiba.core.service.impl;

import org.obiba.core.service.PersistenceManager;
import org.obiba.core.service.PersistenceManagerAware;

/**
 * Abstract helper class for services that have a dependency on the {@link PersistenceManager}.
 */
abstract public class PersistenceManagerAwareService implements PersistenceManagerAware {

  protected PersistenceManager persistenceManager;

  @Override
  public void setPersistenceManager(PersistenceManager persistenceManager) {
    this.persistenceManager = persistenceManager;
  }

  public PersistenceManager getPersistenceManager() {
    return persistenceManager;
  }

}
