package org.obiba.wicket.markup.html.panel;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * A panel with a checkbox inside (typically for checkable list rows).
 * @author ymarcon
 *
 */
public class CheckBoxPanel extends Panel {

  private static final long serialVersionUID = 1234234234L;
  
  private CheckBox checkBox;
  
  public CheckBoxPanel(String id) {
    super(id, new Model(""));
    init();
  }
  
  public CheckBoxPanel(String id, IModel model) {
    super(id, model);
    init();
  }
  
  private void init() {
    checkBox = new CheckBox("checker", getModel());
    add(checkBox);
  }

  @Override
  public Component add(IBehavior behavior) {
    return checkBox.add(behavior);
  }
  
}
