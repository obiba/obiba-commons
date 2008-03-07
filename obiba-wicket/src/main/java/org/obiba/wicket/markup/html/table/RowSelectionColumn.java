package org.obiba.wicket.markup.html.table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeaderlessColumn;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.obiba.wicket.markup.html.panel.CheckBoxPanel;
import org.obiba.wicket.markup.html.table.EntityListTablePanel.EntitySelection;


public class RowSelectionColumn extends HeaderlessColumn {

  private static final long serialVersionUID = 1L;

  private EntityListTablePanel<?> table;
  
  private List<CheckBoxPanel> checkboxes = new ArrayList<CheckBoxPanel>();

  public RowSelectionColumn(EntityListTablePanel<?> table) {
    this.table = table;
  }

  @Override
  public String getCssClass() {
    return "rowSelector";
  }

  @SuppressWarnings("unchecked")
  public void populateItem(Item item, String componentId, IModel model) {
    final EntitySelection selection = table.getSelection(model);

    final CheckBoxPanel cbPanel = new CheckBoxPanel(componentId, new PropertyModel(selection, "selected"));
    cbPanel.setOutputMarkupId(true);
    cbPanel.add(new AjaxEventBehavior("onclick") {

      private static final long serialVersionUID = 1L;

      @Override
      protected void onEvent(AjaxRequestTarget target) {
        selection.setSelected(!selection.getSelected());
      }

    });
    item.add(cbPanel);
    checkboxes.add(cbPanel);
  }

  @Override
  public Component getHeader(String componentId) {
    CheckBoxPanel cbPanel = new CheckBoxPanel(componentId, new PropertyModel(table, "allSelected"));
    cbPanel.add(new AjaxEventBehavior("onclick") {

      private static final long serialVersionUID = 1L;

      @Override
      protected void onEvent(AjaxRequestTarget target) {
        table.setAllSelected(!table.getAllSelected());
        boolean selected = table.getAllSelected();
        for (Serializable id : table.getSelections().keySet()) {
          table.getSelections().get(id).setSelected(selected);
        }
        for (CheckBoxPanel cb : checkboxes)
          target.addComponent(cb);
      }

    });
    return cbPanel;
  }
  
  public void clearSelectionComponents() {
    checkboxes.clear();
  }

}
