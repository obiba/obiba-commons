/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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

  @Override
  protected String newValue(String currentValue, String replacementValue) {
    String result = "return confirm('" + replacementValue + "')";
    if(currentValue != null) {
      result = currentValue + "; " + result;
    }
    return result;
  }
}
