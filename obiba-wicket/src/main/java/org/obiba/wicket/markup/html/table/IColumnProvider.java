package org.obiba.wicket.markup.html.table;

import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;

/**
 * A column provider will provide the columns to be displayed for an EntityListTable.
 *
 * @param <T> the class of the entity that will be represented in the generated table.
 */
public interface IColumnProvider<T> {

  List<String> getColumnHeaderNames();

  List<IColumn<T>> getRequiredColumns();

  List<IColumn<T>> getDefaultColumns();

  List<IColumn<T>> getAdditionalColumns();

}
