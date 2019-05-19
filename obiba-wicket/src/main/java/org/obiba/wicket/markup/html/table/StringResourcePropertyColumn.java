/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.wicket.markup.html.table;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

/**
 * A specialization of {@link PropertyColumn} that uses a {@link StringResourceModel} to localize the value of the property.
 * <p>
 * The typical usage is for enum properties for which we want to localize each value. Given a model <code>Study</code>
 * with a property <code>design</code> of type <code>enum StudyDesign</code> with values IN_PROGRESS and COMPLETED,
 * all that is required to localize the values would be to specify a prefix for the resource key to lookup and ad them
 * to one of the bundles. For example, using the prefix <code>StudyDesign</code> and <code>design</code> as the property expression,
 * the generated key would be <code>StudyDesign.${design}</code>. Refer to the {@link StringResourceModel} for more information.
 * </p>
 */
public class StringResourcePropertyColumn<T> extends PropertyColumn<T> {

  private static final long serialVersionUID = -1672283569335782243L;

  private final Component component;

  private final String resourceKey;

  public StringResourcePropertyColumn(IModel<String> displayModel, String propertyExpression, Component component,
      String resourceKeyPrefix) {
    super(displayModel, propertyExpression);
    this.component = component;
    resourceKey = resourceKeyPrefix + ".${" + propertyExpression + "}";
  }

  public StringResourcePropertyColumn(IModel<String> displayModel, String sortProperty, String propertyExpression,
      Component component, String resourceKeyPrefix) {
    super(displayModel, sortProperty, propertyExpression);
    this.component = component;
    resourceKey = resourceKeyPrefix + ".${" + propertyExpression + "}";
  }

  @Override
  protected IModel<?> createLabelModel(IModel<T> embeddedModel) {
    IModel<?> propertyModel = super.createLabelModel(embeddedModel);
    if(propertyModel.getObject() != null) {
      return new StringResourceModel(resourceKey, component, embeddedModel, propertyModel.getObject().toString());
    } else {
      return null;
    }
  }

}
