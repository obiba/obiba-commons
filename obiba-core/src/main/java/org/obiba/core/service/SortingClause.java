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
   * @param field the field to sort on.
   * @param asc the direction to sort in.
   */
  public SortingClause(String field, boolean asc) {
    this.field = field;
    this.ascending = asc;
  }

  /**
   * Creates a <code>SortingClause</code> on the specified field in the specified direction.
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
    return this.field;
  }

  public boolean isAscending() {
    return this.ascending;
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
