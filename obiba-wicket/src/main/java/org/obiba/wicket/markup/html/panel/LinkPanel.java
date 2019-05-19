/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.wicket.markup.html.panel;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.ILinkListener;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

public class LinkPanel extends Panel {

  private static final long serialVersionUID = -3959770015071789778L;

  private static final String LINK_ID = "link";

  private static final String LABEL_ID = "label";

  public LinkPanel(String id, Class<? extends Page> pageClass, PageParameters params, IModel labelModel) {
    super(id, labelModel);
    BookmarkablePageLink link = new BookmarkablePageLink(LINK_ID, pageClass, params);
    link.add(makeLabelComponent(labelModel));
    add(link);
  }

  public LinkPanel(String id, IModel labelModel, Class<? extends Page> pageClass, String paramName,
      IModel paramValueModel) {
    super(id, labelModel);

    PageParameters params = new PageParameters();
    Object paramValue = paramValueModel.getObject();
    if(paramValue != null) {
      params.add(paramName, paramValue.toString());
    }
    BookmarkablePageLink link = new BookmarkablePageLink(LINK_ID, pageClass, params);
    link.add(makeLabelComponent(labelModel));
    add(link);

    if(paramValue == null) {
      link.setEnabled(false);
    }
  }

  public LinkPanel(String id, final ILinkListener listener, IModel labelModel) {
    super(id, labelModel);

    Link link = new Link(LINK_ID) {

      private static final long serialVersionUID = 1L;

      @Override
      public void onClick() {
        listener.onLinkClicked();
      }

    };
    link.add(makeLabelComponent(labelModel));
    add(link);
  }

  public LinkPanel(String id, IModel linkModel, IModel labelModel) {
    this(id, linkModel, labelModel, true);
  }

  public LinkPanel(String id, IModel linkModel, IModel labelModel, boolean newWindow) {
    super(id, labelModel);
    ExternalLink link = new ExternalLink(LINK_ID, linkModel);
    if(newWindow) {
      link.add(new SimpleAttributeModifier("target", "_blank"));
    }
    link.add(makeLabelComponent(labelModel));
    add(link);
  }

  protected Label makeLabelComponent(IModel labelModel) {
    Label label = new Label(LABEL_ID, labelModel);
    // removes the span tag
    label.setRenderBodyOnly(true);
    return label;
  }

}
