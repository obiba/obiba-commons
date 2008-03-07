package org.obiba.core.service;

import java.io.Serializable;
import java.util.List;

public interface EntityQueryService {

  public <T> T get(Class<T> type, Serializable id);
  
  public Serializable getId(Object o);

  public <T> List<T> list(Class<T> type, PagingClause paging, SortingClause ... clauses);

  public <T> List<T> list(Class<T> type, SortingClause ... clauses);

  public int count(Class<?> type);

  public <T> List<T> match(T template, PagingClause paging, SortingClause ... clauses);

  public <T> List<T> match(T template, SortingClause ... clauses);

  public <T> T matchOne(T template, SortingClause ... clauses);

  public int count(Object template);

  public <T> T refresh(T entity);

}
