package org.obiba.wicket.markup.html.table;

import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.link.ILinkListener;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.obiba.wicket.markup.html.panel.LinkPanel;

abstract public class LinkPropertyColumn extends PropertyColumn {

  public LinkPropertyColumn(IModel displayModel, String sortProperty, String propertyExpression) {
    super(displayModel, sortProperty, propertyExpression);
  }

  public LinkPropertyColumn(IModel displayModel, String propertyExpression) {
    super(displayModel, propertyExpression);
  }

  @Override
  public void populateItem(Item item, String componentId, final IModel model) {
    IModel labelModel = createLabelModel(model);
    item.add(new LinkPanel(componentId, new ILinkListener() {

      private static final long serialVersionUID = 8704288547689782855L;

      public void onLinkClicked() {
        onClick(model);
      }
    }, labelModel));
  }

  abstract protected void onClick(IModel model);

}
