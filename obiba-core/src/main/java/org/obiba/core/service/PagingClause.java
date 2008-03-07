package org.obiba.core.service;

public class PagingClause {

  private int offset = -1;
  private int limit = -1;

  public PagingClause(int offset) {
    this(offset, 100);
  }
  
  public PagingClause(int offset, int limit) {
    this.offset = offset;
    this.limit = limit;
  }

  static public PagingClause create(int offset, int limit) {
    return new PagingClause(offset, limit);
  }

  static public PagingClause create(int offset) {
    return new PagingClause(offset);
  }

  public int getLimit() {
    return limit;
  }

  public int getOffset() {
    return offset;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }

  public void setOffset(int offset) {
    this.offset = offset;
  }

  @Override
  public String toString() {
    return "{"+offset+","+limit+"}";
  }
}
