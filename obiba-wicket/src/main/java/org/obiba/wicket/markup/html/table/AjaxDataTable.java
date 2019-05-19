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

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackHeadersToolbar;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxNavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.OddEvenItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

public class AjaxDataTable<T> extends DataTable<T> {

  private static final long serialVersionUID = 1L;

  private final IModel titleModel;

  public AjaxDataTable(String id, IModel titleModel, List<IColumn<T>> columns, ISortableDataProvider<T> dataProvider,
      Component commands, int rowsPerPage) {
    this(id, titleModel, (IColumn[]) columns.toArray(new IColumn[columns.size()]), dataProvider, commands, rowsPerPage);
  }

  public AjaxDataTable(String id, IModel titleModel, IColumn<T>[] columns, ISortableDataProvider<T> dataProvider,
      Component commands, int rowsPerPage) {
    super(id, columns, dataProvider, rowsPerPage);
    setOutputMarkupId(true);
    setVersioned(false);

    this.titleModel = titleModel;

    addTopToolbar(
        new TableTitleToolbar(this, titleModel, new StringResourceModel("element.${number}", this, new Model(this)),
            commands));
    addTopToolbar(new AjaxNavigationToolbar(this));
    addTopToolbar(new AjaxFallbackHeadersToolbar(this, dataProvider));
    addBottomToolbar(new NoRecordsToolbar(this));
  }

  @Override
  protected Item newRowItem(String id, int index, IModel model) {
    return new OddEvenItem(id, index, model);
  }

  public IModel getTitleModel() {
    return titleModel;
  }

  public String getNumber() {
    if(getRowCount() == 1) {
      return "singular";
    }
    return "plural";
  }
}
