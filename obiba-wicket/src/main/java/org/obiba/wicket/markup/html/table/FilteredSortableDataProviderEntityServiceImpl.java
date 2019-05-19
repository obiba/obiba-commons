/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.wicket.markup.html.table;

import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.obiba.core.service.EntityQueryService;
import org.obiba.core.service.PagingClause;
import org.obiba.core.service.SortingClause;

/**
 * Uses an instance of {@link EntityService} and a template entity to filter records for {@link SortableDataProvider} implementation.
 * <p>
 * The {@link SortParam} is converted to a {@link SortingClause} instance without any modification to the property name.
 * </p>
 */
public class FilteredSortableDataProviderEntityServiceImpl<T> extends SortableDataProviderEntityServiceImpl<T> {

  private static final long serialVersionUID = 1L;

  private final T template;

  public FilteredSortableDataProviderEntityServiceImpl(EntityQueryService queryService, T template) {
    super(queryService, (Class<T>) template.getClass());
    this.template = template;
  }

  @Override
  public Iterator<T> iterator(int first, int count) {
    SortParam sp = getSort();
    SortingClause sort = null;
    if(sp != null) {
      sort = SortingClause.create(sp.getProperty(), sp.isAscending());
    }
    return getList(PagingClause.create(first, count), sort).iterator();
  }

  @Override
  protected List<T> getList(PagingClause paging, SortingClause... clauses) {
    return getQueryService().match(template, paging, clauses);
  }

  @Override
  public int size() {
    return getQueryService().count(template);
  }

}
