/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.wicket.markup.html.panel;

import org.apache.wicket.Page;
import org.apache.wicket.model.PropertyModel;

public class EntityDetailPageLinkPanel extends LinkPanel {

  private static final long serialVersionUID = -3959770015071789778L;

  public EntityDetailPageLinkPanel(String id, Object entity, String labelPropertyName, Class<? extends Page> pageClass,
      String pageParamName, String paramValuePropertyName) {
    super(id, new PropertyModel(entity, labelPropertyName), pageClass, pageParamName,
        new PropertyModel(entity, paramValuePropertyName));
  }

}
