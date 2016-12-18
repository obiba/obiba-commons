/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.core.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.obiba.core.service.PersistenceManager;
import org.obiba.core.validation.exception.ValidationRuntimeException;
import org.obiba.core.validation.interceptor.ObjectValidationInspector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;

public abstract class DefaultPersistenceManagerImpl implements PersistenceManager {

  private final Logger log = LoggerFactory.getLogger(getClass());

  protected ObjectValidationInspector objectValidationInspector;

  public ObjectValidationInspector getObjectValidationInspector() {
    return objectValidationInspector;
  }

  public void setObjectValidationInspector(ObjectValidationInspector objectValidationInspector) {
    this.objectValidationInspector = objectValidationInspector;
  }

  public <T> void validate(T entity) throws ValidationRuntimeException {
    if(getObjectValidationInspector() != null) {
      List<Errors> errors = new ArrayList<Errors>();
      getObjectValidationInspector().setEntityQueryService(this);
      getObjectValidationInspector().inspectObject(errors, entity);
      if(errors.size() > 0) {
        log.warn("Validation error(s) found, throwing ValidationException.");
        throw new ValidationRuntimeException(errors);
      }
    }
  }

}
