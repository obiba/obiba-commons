/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.wicket.markup.html.link;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.obiba.wicket.JavascriptEventConfirmation;

/**
 * A link with a confirm message.
 *
 * @author ymarcon
 */
public abstract class ConfirmLink extends Link {

  /**
   * Constructor with confirmation message.
   *
   * @param id
   * @param message
   */
  public ConfirmLink(String id, String message) {
    super(id);
    add(new JavascriptEventConfirmation("onclick", message));
  }

  /**
   * Constructor with confirmation message and link model.
   *
   * @param id
   * @param message
   * @param model
   */
  public ConfirmLink(String id, String message, IModel model) {
    super(id, model);
    add(new JavascriptEventConfirmation("onclick", message));
  }

  /**
   * Called when link is clicked.
   */
  @Override
  abstract public void onClick();
}
