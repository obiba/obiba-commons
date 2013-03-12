package org.obiba.wicket.markup.html.link;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.Model;
import org.obiba.wicket.markup.html.panel.ConfirmLinkPanel;

/**
 * A list of linkable objects separated by a given separator.
 *
 * @author ymarcon
 */
public abstract class LinkList extends Panel {

  /**
   * List of objects that are linkable, with a comma as default separator.
   *
   * @param id
   * @param links
   */
  public LinkList(String id, List links) {
    this(id, links, ", ");
  }

  /**
   * List of objects that are linkable.
   *
   * @param id
   * @param links
   * @param separator
   */
  public LinkList(String id, List links, final String separator) {
    super(id);

    add(new DataView("list", new ListDataProvider(links)) {

      int count = 0;

      @Override
      protected void populateItem(Item item) {
        final Object obj = item.getModelObject();
        if(obj != null) {
          if(count == 0) item.add(new Label("separator"));
          else item.add(new Label("separator", separator));
          ConfirmLinkPanel link = new ConfirmLinkPanel("link", new Model(getDisplayString(obj))) {

            @Override
            public void onClick() {
              LinkList.this.onClick(obj);
            }

          };
          item.add(link);
          count++;
        }
      }

    });
  }

  /**
   * To be overriden to specify a render different than toString().
   *
   * @param obj
   * @return
   */
  public String getDisplayString(Object obj) {
    if(obj != null) return obj.toString();
    else return "";
  }

  /**
   * Method to implement when a object from the list is clicked.
   *
   * @param obj
   */
  public abstract void onClick(Object obj);

}
