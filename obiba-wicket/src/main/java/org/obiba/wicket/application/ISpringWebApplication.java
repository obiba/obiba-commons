/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.wicket.application;

import org.apache.wicket.spring.ISpringContextLocator;

/**
 * Defines the contract for Spring enabled {@code Application} classes.
 * <p>
 * This interface was created out of the inability to use {@code SpringWebApplication} outside of how it was designed. Code outside of
 * {@code Application} require accessing the {@code ApplicationContext} this interface extracts the required method out
 * of {@code SpringWebApplication} which allows it to be used on classes that do not extend {@code SpringWebApplication}.
 * </p>
 */
public interface ISpringWebApplication {
  ISpringContextLocator getSpringContextLocator();
}
