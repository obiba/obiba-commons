/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
