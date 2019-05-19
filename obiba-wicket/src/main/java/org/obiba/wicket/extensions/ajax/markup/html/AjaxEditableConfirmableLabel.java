/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.wicket.extensions.ajax.markup.html;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

/**
 * An implementation of ajaxified edit-in-place label {@link AbstractAjaxEditableLabel}, with ok/cancel buttons.
 *
 * @author ymarcon
 */
public class AjaxEditableConfirmableLabel extends AjaxEditableLabel {

  private static final long serialVersionUID = 1L;

  private Component ok;

  private Component cancel;

  /**
   * Constructor
   *
   * @param id
   */
  public AjaxEditableConfirmableLabel(String id) {
    super(id);
    setOutputMarkupId(true);
  }

  /**
   * Constructor
   *
   * @param id
   * @param model
   */
  public AjaxEditableConfirmableLabel(String id, IModel model) {
    super(id, model);
    setOutputMarkupId(true);
  }

  protected Component newOkLabel(MarkupContainer parent, String componentId) {
    Label label = new Label(componentId, new StringResourceModel("ok", this, null));
    label.setOutputMarkupId(true);
    label.add(new ValidatorAjaxBehavior(true));
    label.setVisible(false);
    return label;
  }

  protected Component newCancelLabel(MarkupContainer parent, String componentId) {
    Label label = new Label(componentId, new StringResourceModel("cancel", this, null));
    label.setOutputMarkupId(true);
    label.add(new ValidatorAjaxBehavior(false));
    label.setVisible(false);
    return label;
  }

  /**
   * Invoked when the label is in edit mode, and received a cancel event. Typically, nothing
   * should be done here.
   *
   * @param target the ajax request target
   */
  @Override
  protected void onCancel(AjaxRequestTarget target) {
    ok.setVisible(false);
    cancel.setVisible(false);
    super.onCancel(target);
  }

  /**
   * Called when the label is clicked and the component is put in edit mode.
   *
   * @param target Ajax target
   */
  @Override
  protected void onEdit(AjaxRequestTarget target) {
    ok.setVisible(true);
    cancel.setVisible(true);
    super.onEdit(target);
  }

  /**
   * Invoked when the editor was succesfully updated. Use this method e.g. to persist the changed
   * value. This implemention displays the label and clears any window status that might have been
   * set in onError.
   *
   * @param target The ajax request target
   */
  @Override
  protected void onSubmit(AjaxRequestTarget target) {
    ok.setVisible(false);
    cancel.setVisible(false);
    super.onSubmit(target);
  }

  @Override
  protected void initLabelAndEditor(IModel model) {
    super.initLabelAndEditor(model);
    ok = newOkLabel(this, "ok");
    cancel = newCancelLabel(this, "cancel");
    add(ok);
    add(cancel);
  }

  @Override
  protected IBehavior newEditorAjaxBehavior() {
    return new EditorAjaxBehavior() {
      @Override
      protected void onComponentTag(ComponentTag tag) {
        String saveCall = "{" + generateCallbackScript(
            "wicketAjaxGet('" + getCallbackUrl() + "&save=true&'+this.name+'='+wicketEncode(this.value)") +
            "; return false;}";

        String cancelCall = "{" + generateCallbackScript("wicketAjaxGet('" + getCallbackUrl() + "&save=false'") +
            "; return false;}";

        String keypress = "var kc=wicketKeyCode(event); if (kc==27) " + cancelCall +
            " else if (kc!=13) { return true; } else " + saveCall;

        tag.put("onkeypress", keypress);
      }
    };
  }

  protected class ValidatorAjaxBehavior extends EditorAjaxBehavior {

    private static final long serialVersionUID = 1L;

    private final boolean ok;

    /**
     * Constructor.
     */
    public ValidatorAjaxBehavior(boolean ok) {
      this.ok = ok;
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
      String saveCall = "{" + generateCallbackScript(
          "wicketAjaxGet('" + getCallbackUrl() + "&save=true&'+Wicket.$('" + getEditor().getMarkupId() +
              "').name+'='+wicketEncode(Wicket.$('" + getEditor().getMarkupId() + "').value)") + "; return false;}";

      String cancelCall = "{" + generateCallbackScript("wicketAjaxGet('" + getCallbackUrl() + "&save=false'") +
          "; return false;}";

      if(ok) tag.put("onclick", saveCall);
      else tag.put("onclick", cancelCall);
    }
  }
}
