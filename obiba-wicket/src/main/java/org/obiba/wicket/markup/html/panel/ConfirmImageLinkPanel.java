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

import org.apache.wicket.Resource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.obiba.wicket.JavascriptEventConfirmation;

/**
 * A panel with a linked image inside, optionally confirmable.
 *
 * @author ymarcon
 */
public abstract class ConfirmImageLinkPanel extends Panel {

  /**
   * Image with a label and confirm message.
   *
   * @param id
   * @param image
   * @param model
   * @param messageModel
   */
  public ConfirmImageLinkPanel(String id, Resource image, IModel model, IModel messageModel) {
    super(id);
    Link link = new Link("link") {

      private static final long serialVersionUID = 1L;

      @Override
      public void onClick() {
        ConfirmImageLinkPanel.this.onClick();
      }

    };
    link.add(new Image("image", image));
    add(new Label("label", model));
    if(messageModel != null) link.add(new JavascriptEventConfirmation("onclick", messageModel));
    add(link);
  }

  /**
   * Image with no label, no confirm message.
   *
   * @param id
   * @param image
   */
  public ConfirmImageLinkPanel(String id, Resource image) {
    this(id, image, new Model(""));
  }

  /**
   * Image without confirm message.
   *
   * @param id
   * @param image
   * @param model
   */
  public ConfirmImageLinkPanel(String id, Resource image, IModel model) {
    super(id);
    Link link = new Link("link") {

      private static final long serialVersionUID = 1L;

      @Override
      public void onClick() {
        ConfirmImageLinkPanel.this.onClick();
      }

    };
    link.add(new Image("image", image));
    add(new Label("label", model));
    add(link);
  }

  abstract public void onClick();
}
