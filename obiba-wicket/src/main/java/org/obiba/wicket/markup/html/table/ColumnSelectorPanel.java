package org.obiba.wicket.markup.html.table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.obiba.wicket.extensions.ajax.markup.html.AjaxDropDownMultipleChoice;

class ColumnSelectorPanel<T> extends Panel {
  private static final long serialVersionUID = 1L;

  private final EntityListTablePanel<T> table;

  ColumnSelectorPanel(String id, EntityListTablePanel<T> tablePanel) {
    super(id);
    table = tablePanel;
    final ColumnSelectionModel model = new ColumnSelectionModel(table.getColumnProvider());
    add(new AjaxDropDownMultipleChoice("selector", model.getSelectable(), model.getSelected()) {

      private static final long serialVersionUID = 1L;

      @SuppressWarnings("unchecked")
      @Override
      protected void onSelectionUpdate(List selected, Object selection, AjaxRequestTarget target) {
        List<IColumn<T>> columns = new ArrayList<IColumn<T>>(selected.size());
        // Add all the required columns.
        columns.addAll(table.getColumnProvider().getRequiredColumns());
        // Add the selected columns
        for(SelectableColumn selectable : (List<SelectableColumn>) selected) {
          columns.add(selectable.column);
        }
        table.updateColumns(columns, target);
      }
    });
  }

  protected class ColumnSelectionModel extends Model {

    private static final long serialVersionUID = 1L;

    private final List<SelectableColumn> selected;

    private final List<SelectableColumn> unselected;

    private final List<SelectableColumn> selectable;

    public ColumnSelectionModel(IColumnProvider provider) {
      List<IColumn> required = provider.getRequiredColumns();
      List<IColumn> defaultSelected = provider.getDefaultColumns();
      List<IColumn> available = provider.getAdditionalColumns();
      int size = (defaultSelected == null ? 5 : defaultSelected.size()) + (available == null ? 5 : available.size());
      selected = new ArrayList<SelectableColumn>(size);
      unselected = new ArrayList<SelectableColumn>(size);
      selectable = new ArrayList<SelectableColumn>(size);

      int index = 0;
      if(defaultSelected != null) {
        for(IColumn column : defaultSelected) {
          if(required.contains(column)) continue;
          String name = table.getColumnHeaderName(column);
          if(name != null) {
            selected.add(new SelectableColumn(column, index++, true));
          }
        }
      }
      if(available != null) {
        for(IColumn column : available) {
          String name = table.getColumnHeaderName(column);
          if(name != null) {
            unselected.add(new SelectableColumn(column, index++, false));
          }
        }
      }

      selectable.addAll(selected);
      selectable.addAll(unselected);
    }

    public List<SelectableColumn> getSelected() {
      return selected;
    }

    public List<SelectableColumn> getUnselected() {
      return unselected;
    }

    public List<SelectableColumn> getSelectable() {
      return selectable;
    }

    public boolean isSelected(SelectableColumn column) {
      return selected.contains(column);
    }

    public void setSelected(SelectableColumn column) {
      selected.add(column);
      unselected.remove(column);
    }

  }

  protected class SelectableColumn implements Serializable {

    private static final long serialVersionUID = 1L;

    private final IColumn<T> column;

    private final int position;

    private boolean selected = true;

    public SelectableColumn(IColumn<T> column, int position) {
      this(column, position, true);
    }

    public SelectableColumn(IColumn<T> column, int position, boolean selected) {
      this.column = column;
      this.position = position;
      this.selected = selected;
    }

    protected boolean isSelected() {
      return selected;
    }

    protected void setSelected(boolean selected) {
      this.selected = selected;
    }

    protected IColumn<T> getColumn() {
      return column;
    }

    protected int getPosition() {
      return position;
    }

    @Override
    public String toString() {
      return table.getColumnHeaderName(column);
    }

  }

}
