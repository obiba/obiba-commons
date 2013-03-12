package org.obiba.wicket.markup.html.panel;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

/**
 * A panel with a checkbox inside (typically for checkable list rows).
 *
 * @author ymarcon
 */
public class CheckBoxPanel extends Panel {

  private static final long serialVersionUID = 1234234234L;

  private CheckBox checkBox;

  public CheckBoxPanel(String id) {
    super(id, null);
    init();
  }

  public CheckBoxPanel(String id, IModel<Boolean> model) {
    super(id, model);
    init();
  }

  @SuppressWarnings("unchecked")
  private void init() {
    checkBox = new CheckBox("checker", (IModel<Boolean>) getDefaultModel());
    add(checkBox);
  }

  @Override
  public Component add(IBehavior... behavior) {
    return checkBox.add(behavior);
  }

}
