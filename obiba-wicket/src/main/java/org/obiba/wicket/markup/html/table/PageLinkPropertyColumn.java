package org.obiba.wicket.markup.html.table;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.lang.PropertyResolver;
import org.obiba.wicket.markup.html.panel.LinkPanel;


/**
 * A PropertyColumn that creates a bookmarkable link to a Page. Extending classes need to implement the {@link PageLinkPropertyColumn#getPageParameterValue(IModel)}
 * and return the string value of the page parameter used to initialize the Page instance.
 */
public class PageLinkPropertyColumn<T> extends PropertyColumn<T> {

  private static final long serialVersionUID = 2517785311391170622L;

  private Class<? extends Page> pageClass;
  private String key;
  private String propertyExpression;

  /**
   *
   * @param displayModel the label of the column's header
   * @param sortProperty the property used for sorting on this column
   * @param propertyExpression the property expression for displaying the cell's label
   * @param pageClass the Page class that this column links to
   * @param key the page parameter key that is required to instanciate the Page instance
   */
  public PageLinkPropertyColumn(IModel<String> displayModel, String sortProperty, String propertyExpression, Class<? extends Page> pageClass, String key, String valuePropertyExpression) {
    super(displayModel, sortProperty, propertyExpression);
    this.pageClass = pageClass;
    this.key = key;
    this.propertyExpression = valuePropertyExpression;
  }

  public PageLinkPropertyColumn(IModel<String> displayModel, String propertyExpression, Class<? extends Page> pageClass, String key, String valuePropertyExpression) {
    super(displayModel, propertyExpression);
    this.pageClass = pageClass;
    this.key = key;
    this.propertyExpression = valuePropertyExpression;
  }

  @Override
  public void populateItem(Item<ICellPopulator<T>> item, String componentId, IModel<T> rowModel) {
    PageParameters parameters = new PageParameters();
    Object value = PropertyResolver.getValue(propertyExpression, rowModel);
    if(value == null) throw new IllegalArgumentException("Property ["+propertyExpression+"] value is null for object ["+rowModel+"]: cannot create page link.");
    parameters.add(this.key, value.toString());
    item.add(new LinkPanel(componentId, pageClass, parameters, createLabelModel(rowModel)));
  }

//  protected abstract String getPageParameterValue(IModel model);

}
