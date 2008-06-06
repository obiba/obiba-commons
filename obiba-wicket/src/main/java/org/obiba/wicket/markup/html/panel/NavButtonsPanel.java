package org.obiba.wicket.markup.html.panel;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;

/**
 * Draws navigation buttons in a nice row.
 * @author dbujold
 */
public class NavButtonsPanel extends Panel {
  private static final long serialVersionUID = -913918750978211028L;
  
  public static final String LINK_ID = "link";
  public static final String LABEL_ID = "label";
  
  RepeatingView view;

  public NavButtonsPanel(String id) {
    super(id);
    view = new RepeatingView("navButtonsDiv");
    add(view);
  }
  
  /**
   * Adds a button to the navigation button row.
   * @param link the Wicket link. Its id <U>MUST</U> be <tt>NavButtonsPanel.LABEL_ID</tt>.
   * @param label the model with the button's content.
   */
  public void addNavButton(AbstractLink link, IModel label) {
    WebMarkupContainer item = new WebMarkupContainer(view.newChildId());
    view.add(item);
    link.add(new Label(LABEL_ID, label));
    item.add(link);
  }
}
