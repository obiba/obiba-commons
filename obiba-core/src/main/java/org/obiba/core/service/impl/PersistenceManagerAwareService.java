package org.obiba.core.service.impl;

import org.obiba.core.service.PersistenceManager;


public class PersistenceManagerAwareService {

  protected PersistenceManager persistenceManager;

  public void setPersistenceManager(PersistenceManager persistenceManager) {
    this.persistenceManager = persistenceManager;
  }
  
  public PersistenceManager getPersistenceManager() {
    return persistenceManager;
  }

}
