package org.obiba.core.validation.validator;

import org.obiba.core.service.PersistenceManager;

/**
 * Validator class to extend for performing object validation on a specific class, with PersistenceManager
 * for performing persistence checks.
 * 
 * @author ymarcon
 *
 */
public abstract class AbstractPersistenceAwareClassValidator extends AbstractClassValidator {

  protected PersistenceManager persistenceManager;

  public void setPersistenceManager(PersistenceManager persistenceManager) {
    this.persistenceManager = persistenceManager;
  }
  
  public PersistenceManager getPersistenceManager() {
    return persistenceManager;
  }
}
