/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
public class FixedColumnProvider<T> implements IColumnProvider<T>, Serializable {

  private static final long serialVersionUID = -2351067858792714209L;

  private final List<IColumn<T>> fixedColumns;

  public FixedColumnProvider(List<IColumn<T>> fixedColumns) {
    this.fixedColumns = fixedColumns;
  }

  public FixedColumnProvider() {
    fixedColumns = new ArrayList<IColumn<T>>();
  }

  public void addColumn(IColumn<T> column) {
    fixedColumns.add(column);
  }

  @Override
  public List<IColumn<T>> getAdditionalColumns() {
    return null;
  }

  @Override
  public List<String> getColumnHeaderNames() {
    return null;
  }

  @Override
  public List<IColumn<T>> getDefaultColumns() {
    return Collections.unmodifiableList(fixedColumns);
  }

  @Override
  public List<IColumn<T>> getRequiredColumns() {
    return Collections.unmodifiableList(fixedColumns);
  }

}