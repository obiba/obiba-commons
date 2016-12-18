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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeaderlessColumn;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.obiba.wicket.markup.html.panel.CheckBoxPanel;

public class RowSelectionColumn<T> extends HeaderlessColumn<T> {

  private static final long serialVersionUID = 1L;

  private final EntityListTablePanel<T> table;

  private final List<CheckBoxPanel> checkboxes = new ArrayList<CheckBoxPanel>();

  public RowSelectionColumn(EntityListTablePanel<T> table) {
    this.table = table;
  }

  @Override
  public String getCssClass() {
    return "rowSelector";
  }

  @Override
  public void populateItem(Item<ICellPopulator<T>> cellItem, String componentId, IModel<T> rowModel) {
    final EntityListTablePanel<T>.EntitySelection selection = table.getSelection(rowModel);

    CheckBoxPanel cbPanel = new CheckBoxPanel(componentId, new PropertyModel<Boolean>(selection, "selected"));
    cbPanel.setOutputMarkupId(true);
    cbPanel.add(new AjaxEventBehavior("onclick") {

      private static final long serialVersionUID = 1L;

      @Override
      protected void onEvent(AjaxRequestTarget target) {
        selection.setSelected(!selection.getSelected());
      }

    });
    cellItem.add(cbPanel);
    checkboxes.add(cbPanel);
  }

  @Override
  public Component getHeader(String componentId) {
    CheckBoxPanel cbPanel = new CheckBoxPanel(componentId, new PropertyModel<Boolean>(table, "allSelected"));
    cbPanel.add(new AjaxEventBehavior("onclick") {

      private static final long serialVersionUID = 1L;

      @Override
      protected void onEvent(AjaxRequestTarget target) {
        table.setAllSelected(!table.getAllSelected());
        boolean selected = table.getAllSelected();
        for(Serializable id : table.getSelections().keySet()) {
          table.getSelections().get(id).setSelected(selected);
        }
        for(CheckBoxPanel cb : checkboxes)
          target.addComponent(cb);
      }

    });
    return cbPanel;
  }

  public void clearSelectionComponents() {
    checkboxes.clear();
  }

}
