package org.obiba.core.service;

/**
 * Interface to be implemented by any object that wishes to be notified of the {@link PersistenceManager} available in
 * the {@code ApplicationContext} that it runs in.
 * <p/>
 * Note that it is not sufficient to implement this class in order to obtain the {@code PersistenceManager}, the
 * appropriate &lt;property&gt; node must also be specified. Automatic injection requires a {@code
 * BeanFactoryPostProcessor}.
 */
public interface PersistenceManagerAware {

  /**
   * Set the {@code PersistenceManager} available in the {@code ApplicationContext}
   *
   * @param persistenceManager
   */
  void setPersistenceManager(PersistenceManager persistenceManager);

}
