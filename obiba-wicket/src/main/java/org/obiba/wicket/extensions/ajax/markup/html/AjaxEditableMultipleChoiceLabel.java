package org.obiba.wicket.extensions.ajax.markup.html;


import java.io.Serializable;
import java.util.List;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;


/**
 * And inplace editor much like {@link AjaxEditableChoiceLabel}, but now with support for multi line
 * content as its editor.
 * 
 * @author Eelco Hillenius
 */
public class AjaxEditableMultipleChoiceLabel extends AjaxEditableLabel
{
  private static final long serialVersionUID = 1L;

  /** The list of objects. */
  private IModel choices;

  /** The renderer used to generate display/id values for the objects. */
  private IChoiceRenderer renderer;

  /**
   * Construct.
   * 
   * @param id
   *            The component id
   */
  public AjaxEditableMultipleChoiceLabel(String id)
  {
    super(id);
  }

  /**
   * Construct.
   * 
   * @param id
   *            The component id
   * @param model
   *            The model
   */
  public AjaxEditableMultipleChoiceLabel(String id, IModel model)
  {
    super(id, model);
  }

  /**
   * Construct.
   * 
   * @param id
   *            The component id
   * @param choices
   *            The collection of choices in the dropdown
   */
  public AjaxEditableMultipleChoiceLabel(String id, List choices)
  {
    this(id, null, choices);
  }

  /**
   * Construct.
   * 
   * @param id
   *            The component id
   * @param model
   *            The model
   * @param choices
   *            The collection of choices in the dropdown
   */
  public AjaxEditableMultipleChoiceLabel(String id, IModel model, IModel choices)
  {
    super(id, model);
    this.choices = choices;
  }

  /**
   * Construct.
   * 
   * @param id
   *            The component id
   * @param model
   *            The model
   * @param choices
   *            The collection of choices in the dropdown
   * @param renderer
   *            The rendering engine
   */
  public AjaxEditableMultipleChoiceLabel(String id, IModel model, IModel choices, IChoiceRenderer renderer)
  {
    super(id, model);
    this.choices = choices;
    this.renderer = renderer;
  }

  /**
   * Construct.
   * 
   * @param id
   *            The component id
   * @param model
   *            The model
   * @param choices
   *            The collection of choices in the dropdown
   */
  public AjaxEditableMultipleChoiceLabel(String id, IModel model, List choices)
  {
    this(id, model, new Model((Serializable)choices));
  }

  /**
   * Construct.
   * 
   * @param id
   *            The component id
   * @param model
   *            The model
   * @param choices
   *            The collection of choices in the dropdown
   * @param renderer
   *            The rendering engine
   */
  public AjaxEditableMultipleChoiceLabel(String id, IModel model, List choices, IChoiceRenderer renderer)
  {
    this(id, model, new Model((Serializable)choices), renderer);
  }

  /**
   * @see org.apache.wicket.extensions.ajax.markup.html.AjaxEditableLabel#newEditor(org.apache.wicket.MarkupContainer,
   *      java.lang.String, org.apache.wicket.model.IModel)
   */
  protected FormComponent newEditor(MarkupContainer parent, String componentId, IModel model)
  {
    ListMultipleChoice editor = new ListMultipleChoice(componentId, model, new AbstractReadOnlyModel() {

      private static final long serialVersionUID = 1L;

      @Override
      public Object getObject() {
        return choices.getObject();
      }

    }, renderer);
    if (getEditorSize()>0)
      editor.setMaxRows(getEditorSize());
    editor.setOutputMarkupId(true);
    editor.setVisible(false);
    editor.add(new EditorAjaxBehavior()
    {
      private static final long serialVersionUID = 1L;

      protected void onComponentTag(ComponentTag tag)
      {
        super.onComponentTag(tag);
        final String saveCall = "{wicketAjaxGet('" + getCallbackUrl() +
        "&save=true&'+wicketSerialize(this)); return true;}";

        final String cancelCall = "{wicketAjaxGet('" + getCallbackUrl() + 
        "&save=false'); return false;}";

        final String keypress = "var kc=wicketKeyCode(event); if (kc==27) " + cancelCall +
        " else if (kc!=13) { return true; } else " + saveCall;

        tag.put("onblur", saveCall);
        tag.put("onkeypress", keypress);
      }
    });
    return editor;
  }
}
