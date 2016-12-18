/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.core.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

final public class StringUtil {

  private StringUtil() {
  }

  static public String collectionToString(Collection<?> c, String separator) {
    if(c != null) {
      StringBuilder sb = new StringBuilder();
      for(Iterator<?> iter = c.iterator(); iter.hasNext(); ) {
        Object o = (Object) iter.next();
        sb.append(o);
        if(iter.hasNext()) sb.append(separator);
      }
      return sb.toString();
    }
    return null;
  }

  static public String collectionToString(Collection<?> c) {
    return collectionToString(c, ",");
  }

  static public String stringArrayToString(String[] array, String separator) {
    if(array != null) {
      StringBuilder sb = new StringBuilder();
      for(int i = 0; i < array.length; i++) {
        //Add separator if this element is not the first in the list.
        if(i > 0) {
          sb.append(separator);
        }
        sb.append(array[i]);
      }
      return sb.toString();
    }
    return null;
  }

  static public String stringArrayToString(String[] pArray) {
    return stringArrayToString(pArray, ",");
  }

  static public String arrayToString(Object... objects) {
    if(objects != null) return Arrays.toString(objects);
    return "null";
  }

  static public String ellipsis(String value, int maxSize) {
    if(value != null && value.length() > maxSize) {
      return value.substring(0, maxSize - 3) + "...";
    }
    return value;
  }

  /**
   * Returns an Object for which the toString() method calls {@link StringUtil#arrayToString(Object...)}.
   * This method is useful for logging messages that contain array parameters.
   *
   * @param objects the array of objects for which to defer the toString call.
   * @return an Object that calls {@link StringUtil#arrayToString(Object...)} when its toString method is called.
   */
  static public Object deferToString(Object... objects) {
    return new DeferToString(objects);
  }

  /**
   * Keeps a reference to an array of objects to defer a toString() call on them, also caches the resulting string.
   */
  static private class DeferToString {

    final private Object[] objects;

    private String result;

    DeferToString(Object[] objects) {
      this.objects = objects;
    }

    public String toString() {
      return result != null ? result : (result = arrayToString(objects));
    }
  }

}
