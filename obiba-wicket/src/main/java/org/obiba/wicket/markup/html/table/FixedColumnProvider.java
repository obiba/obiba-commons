/**
 * 
 */
package org.obiba.wicket.markup.html.table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;

/**
 * An implementation of {@code IColumnProvider} for a fixed list of columns. Specifically, this implementation does not
 * provide any additional columns and all specified columns are shown by default and required.
 */
public class FixedColumnProvider implements IColumnProvider, Serializable {

  private static final long serialVersionUID = -2351067858792714209L;

  private final List<IColumn> fixedColumns;

  public FixedColumnProvider(final List<IColumn> fixedColumns) {
    this.fixedColumns = fixedColumns;
  }

  public FixedColumnProvider() {
    this.fixedColumns = new ArrayList<IColumn>();
  }

  public void addColumn(IColumn column) {
    fixedColumns.add(column);
  }

  public List<IColumn> getAdditionalColumns() {
    return null;
  }

  public List<String> getColumnHeaderNames() {
    return null;
  }

  public List<IColumn> getDefaultColumns() {
    return Collections.unmodifiableList(fixedColumns);
  }

  public List<IColumn> getRequiredColumns() {
    return Collections.unmodifiableList(fixedColumns);
  }

}