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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public class TableTitleToolbar extends AbstractToolbar {

  private static final long serialVersionUID = -9147214207355639443L;

  public TableTitleToolbar(DataTable<?> table, IModel<?> titleModel, IModel<?> countModel, Component commands) {
    super(table);

    WebMarkupContainer span = new WebMarkupContainer("span");
    add(span);
    span.add(new AttributeModifier("colspan", true, new Model<String>(String.valueOf(table.getColumns().length))));

    span.add(new Label("titleLabel", titleModel));
    span.add(new Label("listCount", countModel));
    if(!"commands".equals(commands.getId())) {
      throw new IllegalArgumentException("Command panel's ID must be 'commands'");
    }
    span.add(commands);
  }

}
