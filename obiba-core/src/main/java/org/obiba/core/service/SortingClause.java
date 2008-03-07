package org.obiba.core.service;


public class SortingClause {

  private String field = null;
  private boolean ascending = true;

  public SortingClause(String field) {
    this(field, true);
  }
  
  public SortingClause(String field, boolean asc) {
    this.field = field;
    this.ascending = asc;
  }

  static public SortingClause create(String field, boolean asc) {
    return new SortingClause(field, asc);
  }

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
  public String toString() {
    return "{" + field + (ascending ? " asc" : " desc") + "}";
  }

}
