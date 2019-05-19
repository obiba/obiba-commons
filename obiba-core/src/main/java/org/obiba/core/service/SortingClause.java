/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.core.service;

/**
 * An instance of this class enables sorting results from {@link EntityQueryService}.
 */
final public class SortingClause {

  /**
   * The field the sorting clause applies to
   */
  private String field = null;

  /**
   * Whether sorting is done ascending (default) or descending.
   */
  private boolean ascending = true;

  /**
   * Creates an ascending <code>SortingClause</code> on the specified field.
   *
   * @param field the field to sort on.
   */
  public SortingClause(String field) {
    this(field, true);
  }

  /**
   * Creates a <code>SortingClause</code> on the specified field in the specified direction.
   *
   * @param field the field to sort on.
   * @param asc the direction to sort in.
   */
  public SortingClause(String field, boolean asc) {
    this.field = field;
    ascending = asc;
  }

  /**
   * Creates a <code>SortingClause</code> on the specified field in the specified direction.
   *
   * @param field the field to sort on.
   * @param asc the direction to sort in.
   */
  static public SortingClause create(String field, boolean asc) {
    return new SortingClause(field, asc);
  }

  /**
   * Creates an ascending <code>SortingClause</code> on the specified field.
   *
   * @param field the field to sort on.
   */
  static public SortingClause create(String field) {
    return new SortingClause(field);
  }

  public String getField() {
    return field;
  }

  public boolean isAscending() {
    return ascending;
  }

  @Override
  public boolean equals(Object obj) {
    if(obj instanceof SortingClause) {
      SortingClause rhs = (SortingClause) obj;
      return field.equals(rhs.field) && ascending == rhs.ascending;
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 37 * result + field.hashCode();
    return 37 * result + (ascending ? 0 : 1);
  }

  @Override
  public String toString() {
    return "{" + field + (ascending ? " asc" : " desc") + "}";
  }

}
