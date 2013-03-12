package org.obiba.wicket.markup.html.panel;

import org.apache.wicket.Page;
import org.apache.wicket.model.PropertyModel;

public class EntityDetailPageLinkPanel extends LinkPanel {

  private static final long serialVersionUID = -3959770015071789778L;

  public EntityDetailPageLinkPanel(String id, Object entity, String labelPropertyName, Class<? extends Page> pageClass,
      String pageParamName, String paramValuePropertyName) {
    super(id, new PropertyModel(entity, labelPropertyName), pageClass, pageParamName,
        new PropertyModel(entity, paramValuePropertyName));
  }

}
