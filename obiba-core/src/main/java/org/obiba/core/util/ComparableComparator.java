/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.core.util;

import java.io.Serializable;
import java.util.Comparator;

/**
 * An implementation of {@code Comparator} the delegates back to the {@code Comparable} interface.
 * <p>
 * This utility class is useful in situations where a bean uses a configurable {@code Comparator} instance that
 * ultimately compares {@code Comparable} instances. This class could be used as the default comparing strategy.
 * </p>
 * @param <T> a type that implements {@code Comparable}
 * @author plaflamm
 */
public class ComparableComparator<T extends Comparable<T>> implements Comparator<T>, Serializable {

  private static final long serialVersionUID = -2271811769930959756L;

  @Override
  public int compare(T o1, T o2) {
    return o1.compareTo(o2);
  }

}
