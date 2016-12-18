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
