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

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.obiba.wicket.JavascriptEventConfirmation;

/**
 * A panel with a link inside, optionally confirmable.
 *
 * @author ymarcon
 */
public abstract class ConfirmLinkPanel extends Panel {

  /**
   * Constructor with a link and a confirmation message model.
   *
   * @param id
   * @param model
   * @param messageModel
   */
  @SuppressWarnings("serial")
  public ConfirmLinkPanel(String id, IModel model, IModel messageModel) {
    super(id);
    Link link = new Link("link") {

      @Override
      public void onClick() {
        ConfirmLinkPanel.this.onClick();
      }

    };
    link.add(new Label("label", model));
    link.add(new JavascriptEventConfirmation("onclick", messageModel));
    add(link);
  }

  /**
   * Constructor with a link.
   *
   * @param id
   * @param model
   */
  @SuppressWarnings("serial")
  public ConfirmLinkPanel(String id, IModel model) {
    super(id);
    Link link = new Link("link") {

      @Override
      public void onClick() {
        ConfirmLinkPanel.this.onClick();
      }

    };
    link.add(new Label("label", model));
    add(link);
  }

  /**
   * Called when link is clicked.
   */
  abstract public void onClick();
}
