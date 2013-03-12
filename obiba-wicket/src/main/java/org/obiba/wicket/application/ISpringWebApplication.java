package org.obiba.wicket.application;

import org.apache.wicket.spring.ISpringContextLocator;

/**
 * Defines the contract for Spring enabled {@code Application} classes.
 * <p/>
 * This interface was created out of the inability to use {@code SpringWebApplication} outside of how it was designed. Code outside of
 * {@code Application} require accessing the {@code ApplicationContext} this interface extracts the required method out
 * of {@code SpringWebApplication} which allows it to be used on classes that do not extend {@code SpringWebApplication}.
 */
public interface ISpringWebApplication {
  ISpringContextLocator getSpringContextLocator();
}
