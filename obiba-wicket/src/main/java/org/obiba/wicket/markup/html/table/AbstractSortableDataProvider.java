/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.wicket.markup.html.table;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.obiba.core.service.PagingClause;
import org.obiba.core.service.SortingClause;

/**
 * Helper class that converts the {@link SortParam} to a {@link SortingClause} instance without any modification to the
 * property name.
 */
abstract public class AbstractSortableDataProvider<T> extends SortableDataProvider<T> {

  private static final long serialVersionUID = 1L;

  public AbstractSortableDataProvider() {
  }

  @Override
  public Iterator<T> iterator(int first, int count) {
    SortParam sp = getSort();
    SortingClause[] sort = null;
    if(sp != null) {
      // Split on comma and whitespace
      String[] properties = sp.getProperty().split("[,\\s]");
      List<SortingClause> list = new ArrayList<SortingClause>(properties.length);
      for(int i = 0; i < properties.length; i++) {
        if(properties[i].trim().length() > 0) {
          list.add(SortingClause.create(properties[i], sp.isAscending()));
        }
      }
      sort = list.toArray(new SortingClause[0]);
    }
    return getList(PagingClause.create(first, count), sort).iterator();
  }

  @Override
  public IModel<T> model(T object) {
    return makeModel(object);
  }

  abstract protected List<T> getList(PagingClause paging, SortingClause... clauses);

  @Override
  abstract public int size();

  abstract protected IModel<T> makeModel(T object);

}
