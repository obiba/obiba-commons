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

/**
 * Interface to be implemented by any object that wishes to be notified of the {@link PersistenceManager} available in
 * the {@code ApplicationContext} that it runs in.
 * <p>
 * Note that it is not sufficient to implement this class in order to obtain the {@code PersistenceManager}, the
 * appropriate &lt;property&gt; node must also be specified. Automatic injection requires a {@code
 * BeanFactoryPostProcessor}.
 * </p>
 */
public interface PersistenceManagerAware {

  /**
   * Set the {@code PersistenceManager} available in the {@code ApplicationContext}
   *
   * @param persistenceManager
   */
  void setPersistenceManager(PersistenceManager persistenceManager);

}
