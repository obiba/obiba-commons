package org.obiba.core.validation.validator;

import org.obiba.core.service.EntityQueryService;

/**
 * Validator class to extend for performing object validation on a specific class, with PersistenceManager
 * for performing persistence checks.
 * 
 * @author ymarcon
 *
 */
public abstract class AbstractPersistenceAwareClassValidator extends AbstractClassValidator {

  protected EntityQueryService entityQueryService;
  
  public EntityQueryService getEntityQueryService() {
    return entityQueryService;
  }

  public void setEntityQueryService(EntityQueryService entityQueryService) {
    this.entityQueryService = entityQueryService;
  }
}
