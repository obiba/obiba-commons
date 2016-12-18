package org.obiba.wicket.extensions.ajax.markup.html;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.JavascriptUtils;
import org.apache.wicket.validation.IValidator;

/**
 * An implementation of ajaxified edit-in-place component using a {@link TextField} as it's editor.
 * <p>
 * There are several methods that can be overriden for customization.
 * <ul>
 * <li>{@link #onEdit(AjaxRequestTarget)} is called when the label is clicked and the editor is to
 * be displayed. The default implementation switches the label for the editor and places the curret
 * at the end of the text. </li>
 * <li>{@link #onSubmit(AjaxRequestTarget)} is called when in edit mode, the user submitted new
 * content, that content validated well, and the model value succesfully updated. This
 * implementation also clears any <code>window.status</code> set. </li>
 * <li>{@link #onError(AjaxRequestTarget)} is called when in edit mode, the user submitted new
 * content, but that content did not validate. Get the current input by calling
 * {@link FormComponent#getInput()} on {@link #getEditor()}, and the error message by calling:
 * <pre>
 * String errorMessage = editor.getFeedbackMessage().getMessage();
 * </pre>
 * The default implementation of this method displays the error message in
 * <code>window.status</code>, redisplays the editor, selects the editor's content and sets the
 * focus on it.
 * <li>{@link #onCancel(AjaxRequestTarget)} is called when in edit mode, the user choose not to
 * submit the contents (he/she pressed espace). The default implementation displays the label again
 * without any further action.</li>
 * </ul>
 * </p>
 *
 * @author Igor Vaynberg (ivaynberg)
 * @author Eelco Hillenius
 */
public class AjaxEditableLabel<T> extends Panel {

  private static final long serialVersionUID = 1L;

  private int editorSize = -1;

  /**
   * label component.
   */
  private Component label;

  /**
   * editor component.
   */
  private FormComponent<T> editor;

  public AjaxEditableLabel(String id) {
    super(id);
    setOutputMarkupId(true);
  }

  public AjaxEditableLabel(String id, IModel<T> model) {
    super(id, model);
    setOutputMarkupId(true);
  }

  /**
   * Adds a validator to this form component. A model must be available for this component before
   * Validators can be added. Either add this Component to its parent (already having a Model), or
   * provide one before this call via constructor {@link #AjaxEditableLabel(String, IModel)} or
   * {@link #setModel(IModel)}.
   *
   * @param validator The validator
   * @return This
   */
  public final AjaxEditableLabel<T> add(IValidator<T> validator) {
    getEditor().add(validator);
    return this;
  }

  /**
   * The value will be made available to the validator property by means of ${label}. It does not
   * have any specific meaning to FormComponent itself.
   *
   * @param labelModel
   * @return this for chaining
   */
  public final AjaxEditableLabel<T> setLabel(IModel<String> labelModel) {
    getEditor().setLabel(labelModel);
    return this;
  }

  /**
   * @see org.apache.wicket.MarkupContainer#setModel(org.apache.wicket.model.IModel)
   */
  public final Component setModel(IModel<T> model) {
    setDefaultModel(model);
    getLabel().setDefaultModel(model);
    getEditor().setDefaultModel(model);
    return this;
  }

  /**
   * Sets the required flag
   *
   * @param required
   * @return this for chaining
   */
  public final AjaxEditableLabel<T> setRequired(boolean required) {
    getEditor().setRequired(required);
    return this;
  }

  /**
   * Sets the type that will be used when updating the model for this component. If no type is
   * specified String type is assumed.
   *
   * @param type
   * @return this for chaining
   */
  public final AjaxEditableLabel<T> setType(Class<T> type) {
    getEditor().setType(type);
    return this;
  }

  /**
   * Set editor size.
   *
   * @param editorSize
   */
  public void setEditorSize(int editorSize) {
    this.editorSize = editorSize;
  }

  /**
   * Get editor size.
   *
   * @return
   */
  protected int getEditorSize() {
    return editorSize;
  }

  /**
   * Create a new form component instance to serve as editor.
   *
   * @param parent The parent component
   * @param componentId Id that should be used by the component
   * @param model The model
   * @return The editor
   */
  protected Component newLabel(MarkupContainer parent, String componentId, IModel<?> model) {
    Label label = new Label(componentId, model) {

      private static final long serialVersionUID = 1L;

      @Override
      protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
        if(getDefaultModelObject() == null) {
          replaceComponentTagBody(markupStream, openTag, defaultNullLabel());
        } else {
          super.onComponentTagBody(markupStream, openTag);
        }
      }
    };
    label.setOutputMarkupId(true);
    label.add(new LabelAjaxBehavior("onclick"));
    return label;
  }

  /**
   * Create a new form component instance to serve as editor.
   *
   * @param parent The parent component
   * @param componentId Id that should be used by the component
   * @param model The model
   * @return The editor
   */
  protected FormComponent<T> newEditor(MarkupContainer parent, String componentId, IModel<T> model) {
    TextField<T> editor = new TextField<T>(componentId, model) {
      private static final long serialVersionUID = 1L;

      @Override
      protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        if(editorSize > 0) tag.getAttributes().put("size", editorSize);
      }
    };
    editor.setOutputMarkupId(true);
    editor.setVisible(false);
    editor.add(newEditorAjaxBehavior());
    return editor;
  }

  /**
   * Gets the label component.
   *
   * @return The label component
   */
  protected final Component getLabel() {
    return label;
  }

  protected void setLabel(Component label) {
    this.label = label;
  }

  /**
   * Gets the editor component.
   *
   * @return The editor component
   */
  protected final FormComponent<T> getEditor() {
    if(editor == null) {
      initLabelAndEditor(getParentModel());
    }
    return editor;
  }

  protected void setEditor(FormComponent<T> editor) {
    this.editor = editor;
  }

  /**
   * Called when the label is clicked and the component is put in edit mode.
   *
   * @param target Ajax target
   */
  protected void onEdit(AjaxRequestTarget target) {
    label.setVisible(false);
    editor.setVisible(true);
    target.addComponent(this);
    // put focus on the textfield and stupid explorer hack to move the
    // caret to the end
    target.appendJavascript("{ var el=wicketGet('" + editor.getMarkupId() + "');" + "   if (el.createTextRange) { " +
        "     var v = el.value; var r = el.createTextRange(); " +
        "     r.moveStart('character', v.length); r.select(); } }");
    target.focusComponent(editor);
  }

  /**
   * Invoked when the label is in edit mode, received a new input, but that input didn't validate
   *
   * @param target the ajax request target
   */
  protected void onError(AjaxRequestTarget target) {
    Serializable errorMessage = editor.getFeedbackMessage().getMessage();
    if(errorMessage instanceof String) {
      target.appendJavascript("window.status='" + JavascriptUtils.escapeQuotes((String) errorMessage) + "';");
    }
    String editorMarkupId = editor.getMarkupId();
    target.appendJavascript(editorMarkupId + ".select();");
    target.appendJavascript(editorMarkupId + ".focus();");
    target.addComponent(editor);
  }

  /**
   * Invoked when the editor was succesfully updated. Use this method e.g. to persist the changed
   * value. This implemention displays the label and clears any window status that might have been
   * set in onError.
   *
   * @param target The ajax request target
   */
  protected void onSubmit(AjaxRequestTarget target) {
    label.setVisible(true);
    editor.setVisible(false);
    target.addComponent(this);

    target.appendJavascript("window.status='';");
  }

  /**
   * @return Gets the parent model in case no explicit model was specified.
   */
  @SuppressWarnings("unchecked")
  protected IModel<T> getParentModel() {
    // the #getModel() call below will resolve and assign any inheritable
    // model this component can use. Set that directly to the label and
    // editor so that those components work like this enclosing panel
    // does not exist (must have that e.g. with CompoundPropertyModels)
    IModel<T> m = (IModel<T>) getDefaultModel();

    // check that a model was found
    if(m == null) {
      Component parent = getParent();
      String msg = "No model found for this component, either pass one explicitly or " +
          "make sure an inheritable model is available.";
      if(parent == null) {
        msg += " This component is not added to a parent yet, so if this component " +
            "is supposed to use the model of the parent (e.g. when it uses a " +
            "compound property model), add it first before further configuring " +
            "the component calling methods like e.g. setType and addValidator.";
      }
      throw new IllegalStateException(msg);
    }
    return m;
  }

  /**
   * @see org.apache.wicket.Component#onBeforeRender()
   */
  @Override
  protected void onBeforeRender() {
    super.onBeforeRender();
    // lazily add label and editor
    if(editor == null) {
      initLabelAndEditor(getParentModel());
    }
  }

  /**
   * Invoked when the label is in edit mode, and received a cancel event. Typically, nothing
   * should be done here.
   *
   * @param target the ajax request target
   */
  protected void onCancel(AjaxRequestTarget target) {
    label.setVisible(true);
    editor.setVisible(false);
    target.addComponent(this);
  }

  /**
   * Override this to display a different value when the model object is null. Default is
   * <code>...</code>
   *
   * @return The string which should be displayed when the model object is null.
   */
  protected String defaultNullLabel() {
    return "...";
  }

  /**
   * Lazy initialization of the label and editor components and set tempModel to null.
   *
   * @param model The model for the label and editor
   */
  protected void initLabelAndEditor(IModel<T> model) {
    setEditor(newEditor(this, "editor", model));
    setLabel(newLabel(this, "label", model));
    add(getLabel());
    add(getEditor());
  }

  /**
   * The editor behavior, to be overridden if default editor behavior is to changed.
   *
   * @return
   */
  protected IBehavior newEditorAjaxBehavior() {
    return new EditorAjaxBehavior();
  }

  /**
   * Default editor behavior.
   */
  protected class EditorAjaxBehavior extends AbstractDefaultAjaxBehavior {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public EditorAjaxBehavior() {
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
      super.onComponentTag(tag);
      String saveCall = "{" + generateCallbackScript(
          "wicketAjaxGet('" + getCallbackUrl() + "&save=true&'+this.name+'='+wicketEncode(this.value)") +
          "; return false;}";

      String cancelCall = "{" + generateCallbackScript("wicketAjaxGet('" + getCallbackUrl() + "&save=false'") +
          "; return false;}";

      String keypress = "var kc=wicketKeyCode(event); if (kc==27) " + cancelCall +
          " else if (kc!=13) { return true; } else " + saveCall;

      tag.put("onblur", saveCall);
      tag.put("onkeypress", keypress);

    }

    @Override
    protected void respond(AjaxRequestTarget target) {
      RequestCycle requestCycle = RequestCycle.get();
      boolean save = Boolean.valueOf(requestCycle.getRequest().getParameter("save")).booleanValue();

      if(save) {
        getEditor().processInput();

        if(getEditor().isValid()) {
          onSubmit(target);
        } else {
          onError(target);
        }
      } else {
        onCancel(target);
      }
    }
  }

  protected class LabelAjaxBehavior extends AjaxEventBehavior {

    private static final long serialVersionUID = 1L;

    /**
     * Construct.
     *
     * @param event
     */
    public LabelAjaxBehavior(String event) {
      super(event);
    }

    @Override
    protected void onEvent(AjaxRequestTarget target) {
      onEdit(target);
    }
  }
}
