package org.obiba.wicket.markup.html.table;

import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;

public interface IColumnProvider {

  public List<String> getColumnHeaderNames();

  public List<IColumn> getRequiredColumns();
  
  public List<IColumn> getDefaultColumns();
  
  public List<IColumn> getAdditionalColumns();

}
