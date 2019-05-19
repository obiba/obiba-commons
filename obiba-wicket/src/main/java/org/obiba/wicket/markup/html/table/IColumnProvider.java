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
