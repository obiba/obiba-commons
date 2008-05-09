package org.obiba.wicket.markup.html.link;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;

/**
 * A list of linkable objects separated by a given separator.
 * 
 * @author ymarcon
 * 
 */
public abstract class AjaxLinkList extends Panel {

  /**
   * List of objects that are linkable, with a comma as default separator.
   * 
   * @param id
   * @param links
   */
  public AjaxLinkList(String id, List<IModel> links) {
    this(id, links, ", ");
  }

  /**
   * List of objects that are linkable.
   * 
   * @param id
   * @param links
   * @param separator
   */
  public AjaxLinkList(String id, final List<IModel> links,
      final String separator) {
    super(id);

    add(new DataView("list", new ListDataProvider(links)) {

      private static final long serialVersionUID = 1L;

      @Override
      protected void populateItem(Item item) {
        final IModel model = (IModel) item.getModelObject();
        if (model != null) {
          if (links.indexOf(model) == 0)
            item.add(new Label("separator"));
          else
            item.add(new Label("separator", separator));
          AjaxLink link = new AjaxLink("link", model) {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
              AjaxLinkList.this.onClick(model, target);
            }

          };
          link.add(new Label("label", model));
          item.add(link);
        }
      }

    });
  }

  /**
   * Method to implement when a object from the list is clicked.
   * 
   * @param obj
   */
  public abstract void onClick(IModel model, AjaxRequestTarget target);

}
