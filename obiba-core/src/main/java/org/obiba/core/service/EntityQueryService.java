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

import java.io.Serializable;
import java.util.List;

public interface EntityQueryService {

  <T> T get(Class<T> type, Serializable id);

  Serializable getId(Object o);

  <T> List<T> list(Class<T> type, PagingClause paging, SortingClause... clauses);

  <T> List<T> list(Class<T> type, SortingClause... clauses);

  int count(Class<?> type);

  <T> List<T> match(T template, PagingClause paging, SortingClause... clauses);

  <T> List<T> match(T template, SortingClause... clauses);

  <T> T matchOne(T template, SortingClause... clauses);

  int count(Object template);

  <T> T refresh(T entity);

}
