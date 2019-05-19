/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.core.validation.validator;

import org.obiba.core.service.EntityQueryService;

/**
 * Validator class to extend for performing object validation on a specific class, with PersistenceManager
 * for performing persistence checks.
 *
 * @author ymarcon
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
