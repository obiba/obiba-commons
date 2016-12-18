/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.wicket.extensions.ajax.markup.html;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * And inplace editor much like {@link AjaxEditableLabel}, but now with support for multi line
 * content and a {@link TextArea text area} as its editor.
 *
 * @author Eelco Hillenius
 */
public class AjaxEditableChoiceLabel extends AjaxEditableLabel {
  private static final long serialVersionUID = 1L;

  /**
   * The list of objects.
   */
  private IModel choices;

  /**
   * The renderer used to generate display/id values for the objects.
   */
  private IChoiceRenderer renderer;

  /**
   * Construct.
   *
   * @param id The component id
   */
  public AjaxEditableChoiceLabel(String id) {
    super(id);
  }

  /**
   * Construct.
   *
   * @param id The component id
   * @param model The model
   */
  public AjaxEditableChoiceLabel(String id, IModel model) {
    super(id, model);
  }

  /**
   * Construct.
   *
   * @param id The component id
   * @param choices The collection of choices in the dropdown
   */
  public AjaxEditableChoiceLabel(String id, List choices) {
    this(id, null, choices);
  }

  /**
   * Construct.
   *
   * @param id The component id
   * @param model The model
   * @param choices The collection of choices in the dropdown
   */
  public AjaxEditableChoiceLabel(String id, IModel model, IModel choices) {
    super(id, model);
    this.choices = choices;
  }

  /**
   * Construct.
   *
   * @param id The component id
   * @param model The model
   * @param choices The collection of choices in the dropdown
   * @param renderer The rendering engine
   */
  public AjaxEditableChoiceLabel(String id, IModel model, IModel choices, IChoiceRenderer renderer) {
    super(id, model);
    this.choices = choices;
    this.renderer = renderer;
  }

  /**
   * Construct.
   *
   * @param id The component id
   * @param model The model
   * @param choices The collection of choices in the dropdown
   */
  public AjaxEditableChoiceLabel(String id, IModel model, List choices) {
    this(id, model, new Model((Serializable) choices));
  }

  /**
   * Construct.
   *
   * @param id The component id
   * @param model The model
   * @param choices The collection of choices in the dropdown
   * @param renderer The rendering engine
   */
  public AjaxEditableChoiceLabel(String id, IModel model, List choices, IChoiceRenderer renderer) {
    this(id, model, new Model((Serializable) choices), renderer);
  }

  /**
   * @see org.apache.wicket.extensions.ajax.markup.html.AjaxEditableLabel#newLabel(org.apache.wicket.MarkupContainer,
   *      java.lang.String, org.apache.wicket.model.IModel)
   */
//	protected Component newLabel(MarkupContainer parent, String componentId, IModel model)
//	{
//		MultiLineLabel label = new MultiLineLabel(componentId, model);
//		label.setOutputMarkupId(true);
//		label.add(new LabelAjaxBehavior("onclick"));
//		return label;
//	}

  /**
   * @see org.apache.wicket.extensions.ajax.markup.html.AjaxEditableLabel#newEditor(org.apache.wicket.MarkupContainer,
   *      java.lang.String, org.apache.wicket.model.IModel)
   */
  @Override
  protected FormComponent newEditor(MarkupContainer parent, String componentId, IModel model) {
    DropDownChoice editor = new DropDownChoice(componentId, model, new AbstractReadOnlyModel() {

      private static final long serialVersionUID = 1L;

      @Override
      public Object getObject() {
        return choices.getObject();
      }

    }, renderer) {
      @Override
      protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        if(getEditorSize() > 0) tag.getAttributes().put("size", getEditorSize());
      }
    };
    editor.setOutputMarkupId(true);
    editor.setVisible(false);
    editor.add(new EditorAjaxBehavior() {
      private static final long serialVersionUID = 1L;

      @Override
      protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        String saveCall = "{wicketAjaxGet('" + getCallbackUrl() +
            "&save=true&'+this.name+'='+wicketEncode(this.value)); return true;}";

        String cancelCall = "{wicketAjaxGet('" + getCallbackUrl() +
            "&save=false'); return false;}";

        tag.put("onchange", saveCall);
      }
    });
    return editor;
  }

}
