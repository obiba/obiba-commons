package org.obiba.wicket.markup.html.table;

import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;

/**
 * A column provider will provide the columns to be displayed for an EntityListTable.
 *
 * @param <T> the class of the entity that will be represented in the generated table.
 */
public interface IColumnProvider<T> {

  public List<String> getColumnHeaderNames();

  public List<IColumn<T>> getRequiredColumns();
  
  public List<IColumn<T>> getDefaultColumns();
  
  public List<IColumn<T>> getAdditionalColumns();

}
