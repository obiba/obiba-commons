package org.obiba.wicket.markup.html.table;

import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.obiba.core.service.EntityQueryService;
import org.obiba.core.service.PagingClause;
import org.obiba.core.service.SortingClause;

/**
 * Uses an instance of {@link EntityService} to implement {@link SortableDataProvider}.
 * <p/>
 * The {@link SortParam} is converted to a {@link SortingClause} instance without any modification to the property name.
 */
public class SortableDataProviderEntityServiceImpl<T> extends AbstractSortableDataProvider<T> {

  private static final long serialVersionUID = 1L;

  private final EntityQueryService queryService;

  private final Class<T> type;

  public SortableDataProviderEntityServiceImpl(EntityQueryService queryService, Class<T> type) {
    this.queryService = queryService;
    this.type = type;
  }

  @Override
  protected List<T> getList(PagingClause paging, SortingClause... clauses) {
    return getQueryService().list(type, paging, clauses);
  }

  @Override
  public IModel<T> makeModel(T object) {
    return new DetachableEntityModel<T>(getQueryService(), object);
  }

  @Override
  public int size() {
    return getQueryService().count(type);
  }

  protected EntityQueryService getQueryService() {
    return queryService;
  }

}
