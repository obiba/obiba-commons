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

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

public abstract class AjaxSelectableChoiceLabel extends AjaxEditableChoiceLabel {

  private IChoiceRenderer renderer = null;

  private final List selectable;

  private AjaxDropDownMultipleChoice editor = null;

  public AjaxSelectableChoiceLabel(String id, List selectable, IChoiceRenderer renderer) {
    super(id, new PropertyModel(new Dummy2(), "dummy"), selectable, renderer);
    this.renderer = renderer;
    this.selectable = selectable;
  }

  public AjaxSelectableChoiceLabel(String id, List selectable) {
    this(id, selectable, null);
  }

  @Override
  protected FormComponent newEditor(MarkupContainer parent, String componentId, IModel model) {
    if(editor != null) return editor;

    editor = new AjaxDropDownMultipleChoice(componentId, selectable, null, renderer) {

      @Override
      protected void onSelectionUpdate(List selected, Object selection, AjaxRequestTarget target) {
        AjaxSelectableChoiceLabel.this.onSelectionUpdate(selected, selection, target);
        //editor.setVisible(false);
        if(!editor.isKeepSelection()) onSubmit(target);
      }

      @Override
      protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        if(getEditorSize() > 0) tag.getAttributes().put("size", getEditorSize());
      }

    };
    editor.setVisible(false);
    return editor;
  }

  protected abstract void onSelectionUpdate(List selected, Object selection, AjaxRequestTarget target);

  private AjaxDropDownMultipleChoice getChooser() {
    if(editor == null) newEditor(this, "editor", null);
    return editor;
  }

  /**
   * Set keep selection non null after selection event.
   *
   * @param keepSelection
   */
  public void setKeepSelection(boolean keepSelection) {
    getChooser().setKeepSelection(keepSelection);
  }

  /**
   * Get the selectable objects.
   *
   * @return
   */
  public List getSelectable() {
    return getChooser().getSelectable();
  }

  /**
   * Set the selectable objects.
   *
   * @param selectable
   */
  public void setSelectable(List selectable) {
    getChooser().setSelectable(selectable);
  }

  /**
   * Get the selected objects.
   *
   * @return
   */
  public List getSelected() {
    return getChooser().getSelected();
  }

  /**
   * Set the selected objects.
   *
   * @param selected
   */
  public void setSelected(List selected) {
    getChooser().setSelected(selected);
  }

  /**
   * Set the objects selectable selected up to the given last index included.
   *
   * @param lastIndex
   */
  public void setSelected(int lastIndex) {
    getChooser().setSelected(lastIndex);
  }

  /**
   * Get the last selection.
   *
   * @return
   */
  public Object getSelection() {
    return getChooser().getSelection();
  }

  /**
   * Set the last selection.
   *
   * @param selection
   */
  public void setSelection(Object selection) {
    getChooser().setSelection(selection);
  }

  public static class Dummy2 implements Serializable {
    private Object dummy = null;

    protected Dummy2() {}

    public Object getDummy() {
      return dummy;
    }

    public void setDummy(Object dummy) {
      this.dummy = dummy;
    }
  }
}
