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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.PropertyModel;

/**
 * A dropdown that handles a list of selected objects. Rendering show objects
 * currently selected. Ajax event is called whenever the list of selected objects
 * changes.
 *
 * @author ymarcon
 */
public abstract class AjaxDropDownMultipleChoice extends DropDownChoice {

  private List selected = new ArrayList();

  private List selectable;

  private Object selection;

  private boolean keepSelection = false;

  private final IChoiceRenderer userRenderer;

  /**
   * Constructor with the selectable objects and an object renderer.
   *
   * @param id
   * @param selectable
   * @param renderer
   */
  @SuppressWarnings("serial")
  public AjaxDropDownMultipleChoice(String id, List selectable, List currentSelection, IChoiceRenderer renderer) {
    super(id, selectable);
    setSelectable(selectable);
    setSelected(currentSelection);
    setNullValid(true);
    setModel(new PropertyModel(this, "selection"));
    userRenderer = renderer;
    setChoiceRenderer(new IChoiceRenderer() {

      @Override
      public Object getDisplayValue(Object obj) {
        Object rval;

        if(userRenderer != null) rval = userRenderer.getDisplayValue(obj);
        else if(obj != null) rval = obj.toString();
        else rval = "";

        if(selected.contains(obj)) return "- " + rval;
        else return "+ " + rval;
      }

      @Override
      public String getIdValue(Object obj, int idx) {
        if(userRenderer != null) return userRenderer.getIdValue(obj, idx);
        else if(obj != null) return obj.toString();
        else return "";
      }

    });
    add(new AjaxFormComponentUpdatingBehavior("onchange") {

      @SuppressWarnings("unchecked")
      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        if(selection == null) return;

        // keep or remove selection
        if(selected.contains(selection)) selected.remove(selection);
        else selected.add(selection);

        // order the selected same as the selectable
        List orderedSelected = new ArrayList();
        for(Object obj : getSelectable()) {
          if(selected.contains(obj)) orderedSelected.add(obj);
        }
        selected = orderedSelected;

        onSelectionUpdate(selected, selection, target);
        if(!keepSelection) {
          selection = null;
          target.addComponent(AjaxDropDownMultipleChoice.this);
        }
      }

    });
  }

  public AjaxDropDownMultipleChoice(String id, List selectable, List selected) {
    this(id, selectable, selected, null);
  }

  public AjaxDropDownMultipleChoice(String id, List selectable) {
    this(id, selectable, null, null);
  }

  /**
   * Method called on object selection ajax event.
   *
   * @param language
   * @param target
   */
  protected abstract void onSelectionUpdate(List selected, Object selection, AjaxRequestTarget target);

  /**
   * Keep selection non null after selection event.
   *
   * @return
   */
  protected boolean isKeepSelection() {
    return keepSelection;
  }

  /**
   * Set keep selection non null after selection event.
   *
   * @param keepSelection
   */
  protected void setKeepSelection(boolean keepSelection) {
    this.keepSelection = keepSelection;
  }

  /**
   * Get the selectable objects.
   *
   * @return
   */
  public List getSelectable() {
    return selectable;
  }

  /**
   * Set the selectable objects.
   *
   * @param selectable
   */
  public void setSelectable(List selectable) {
    this.selectable = selectable;
  }

  /**
   * Get the selected objects.
   *
   * @return
   */
  public List getSelected() {
    return selected;
  }

  /**
   * Set the selected objects.
   *
   * @param selected
   */
  public void setSelected(List selected) {
    this.selected = selected;
  }

  /**
   * Set the objects selectable selected up to the given last index included.
   *
   * @param lastIndex
   */
  @SuppressWarnings("unchecked")
  public void setSelected(int lastIndex) {
    if(selected == null || selectable == null) return;

    selected.clear();

    for(int i = 0; i <= lastIndex; i++) {
      if(i < selectable.size()) selected.add(selectable.get(i));
      else return;
    }
  }

  /**
   * Get the last selection.
   *
   * @return
   */
  public Object getSelection() {
    return selection;
  }

  /**
   * Set the last selection.
   *
   * @param selection
   */
  public void setSelection(Object selection) {
    this.selection = selection;
  }

}
