package org.obiba.wicket;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public class JavascriptEventConfirmation extends AttributeModifier {

  private static final long serialVersionUID = 2341232344L;

  public JavascriptEventConfirmation(String event, String msg) {
    super(event, true, new Model(msg));
  }
  
  public JavascriptEventConfirmation(String event, IModel model) {
    super(event, true, model);
  }

  protected String newValue(final String currentValue, final String replacementValue) {
    String result = "return confirm('" + replacementValue + "')";
    if (currentValue != null) {       
      result = currentValue + "; " + result;
    }
    return result;
  }
}
