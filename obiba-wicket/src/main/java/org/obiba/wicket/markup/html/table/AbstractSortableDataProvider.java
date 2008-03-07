package org.obiba.wicket.markup.html.table;

import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.obiba.core.service.PagingClause;
import org.obiba.core.service.SortingClause;


/**
 * Helper class that converts the {@link SortParam} to a {@link SortingClause} instance without any modification to the property name.
 */
abstract public class AbstractSortableDataProvider<T> extends SortableDataProvider {

  private static final long serialVersionUID = 1L;

  public AbstractSortableDataProvider() {
  }

  public Iterator<T> iterator(int first, int count) {
    SortParam sp = getSort();
    SortingClause sort = null;
    if (sp != null) {
      sort = SortingClause.create(sp.getProperty(), sp.isAscending());
    }
    return getList(PagingClause.create(first, count), sort).iterator();
  }

  @SuppressWarnings("unchecked")
  public IModel model(final Object object) {
    return makeModel((T)object);
  }

  abstract protected List<T> getList(PagingClause paging, SortingClause... clauses);

  abstract public int size();
  
  abstract protected IModel makeModel(final T object);
  
}
