/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.wicket.markup.html.panel;

import java.io.Serializable;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authorization.strategies.role.metadata.MetaDataRoleAuthorizationStrategy;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

/**
 * A convenient class for generating tables of key/value pairs.
 * <p>
 * Data is presented in a two column table. The first column contains the keys and the second contains the values. Rows
 * are added by using one of the <code>addRow()</code> methods.
 * </p>
 */
@SuppressWarnings("UnusedDeclaration")
public class KeyValueDataPanel extends Panel {

  private static final long serialVersionUID = 5705222737293170400L;

  RepeatingView view;

  ListView<?> myListView;

  int rowCounter = 0;

  private final WebMarkupContainer noDataRow = new WebMarkupContainer("noDataRow");

  public KeyValueDataPanel(String id) {
    this(id, (Component) null);
  }

  /**
   * @param id
   * @param header use {@link #getHeaderId()} to put the right id to the component
   */
  public KeyValueDataPanel(String id, Component header) {
    super(id);
    view = new RepeatingView("repeating");
    add(view);
    add(new KeyValueDataPanelHeaderFragment(header));
    noDataRow.add(new Label("noDataLabel", new StringResourceModel("datatable.no-records-found", this, null)));
    add(noDataRow);
    noDataRow.setVisible(false);
  }

  public KeyValueDataPanel(String id, IModel<?> header) {
    this(id, new Label(getHeaderId(), header));
  }

  public static String getHeaderId() {
    return "tableLabel";
  }

  public static String getRowKeyId() {
    return "rowKey";
  }

  public static String getRowValueId() {
    return "rowValue";
  }

  /**
   * Set/replace the current header label.
   *
   * @param header
   */
  public void setHeader(IModel<?> header) {
    setHeader(new Label(getHeaderId(), header));
  }

  /**
   * Set/replace the current header.
   *
   * @param header
   */
  public void setHeader(Component header) {
    KeyValueDataPanelHeaderFragment newHeader = new KeyValueDataPanelHeaderFragment(header);
    addOrReplace(newHeader);
  }

  /**
   * When set to true, a row that spans both columns is made visible. Its content is a message that states that no data
   * was found to display. The resource key <code>datatable.no-records-found</code> is used to generate the message.
   *
   * @param show determines whether or not to show the row.
   */
  public void showNoDataRow(boolean show) {
    noDataRow.setVisible(show);
  }

  /**
   * Add labels, with authorization filter.
   *
   * @param key
   * @param value
   * @param rowAuth
   */
  public void addRow(IModel<?> key, IModel<?> value, RowAuthorization... rowAuth) {
    addRow(key, value, false, rowAuth);
  }

  /**
   * Add labels, with key indentation and authorization filter.
   *
   * @param key
   * @param value
   * @param indent
   * @param rowAuth
   */
  public void addRow(IModel<?> key, IModel<?> value, boolean indent, RowAuthorization... rowAuth) {
    addRow(new Label(getRowKeyId(), key), createValueLabel(value), indent, rowAuth);
  }

  /**
   * Add components row, with authorization filter.
   *
   * @param key
   * @param value
   * @param rowAuth
   */
  public void addRow(IModel<?> key, Component value, RowAuthorization... rowAuth) {
    addRow(key, value, false, rowAuth);
  }

  /**
   * Add components row, with key indentation and authorization filter.
   *
   * @param key
   * @param value
   * @param indent
   * @param rowAuth
   */
  public void addRow(IModel<?> key, Component value, boolean indent, RowAuthorization... rowAuth) {
    addRow(new Label(getRowKeyId(), key), value, indent, rowAuth);
  }

  /**
   * Add components row, with authorization filter.
   *
   * @param key
   * @param value
   * @param rowAuth
   */
  public void addRow(Component key, Component value, RowAuthorization... rowAuth) {
    addRow(key, value, false, rowAuth);
  }

  /**
   * Add components row, with authorization filter.
   *
   * @param key
   * @param value
   * @param rowAuth
   */
  public void addRow(Component key, Component value, final boolean indent, RowAuthorization... rowAuth) {
    // If the value's is empty, generate an empty component with a spacing character.
    // This prevents some display glitches on the table.
    Component safeValue = value == null ? new EmptyCellFragment(getRowValueId()) : value;

    rowCounter++;
    WebMarkupContainer item = new WebMarkupContainer(view.newChildId());
    view.add(item);

    item.add(key);

    key.add(new AttributeModifier("class", true, new AbstractReadOnlyModel() {
      private static final long serialVersionUID = 825034630638663648L;

      @Override
      public Object getObject() {
        return indent ? getKeyIndentCssClass() : getKeyCssClass();
      }
    }));

    item.add(safeValue);

    if(rowAuth != null) {
      for(RowAuthorization aRowAuth : rowAuth) {
        MetaDataRoleAuthorizationStrategy.authorize(item, aRowAuth.getAction(), aRowAuth.getRoles());
      }
    }

    safeValue.add(new AttributeModifier("class", true, new AbstractReadOnlyModel() {
      private static final long serialVersionUID = 8250197630638663648L;

      int rowIndex = rowCounter;

      @Override
      public Object getObject() {
        return rowIndex % 2 == 1 ? getValueOddCssClass() : getValueEvenCssClass();
      }
    }));

  }

  protected String getValueOddCssClass() {
    return "keyValueTableValueOdd";
  }

  protected String getValueEvenCssClass() {
    return "keyValueTableValueEven";
  }

  protected String getKeyCssClass() {
    return "keyValueTableKey";
  }

  protected String getKeyIndentCssClass() {
    return "keyValueTableKeyIndent";
  }

  public static class RowAuthorization {
    private final Action action;

    private final String roles;

    public RowAuthorization(Action action, String roles) {
      this.action = action;
      this.roles = roles;
    }

    public Action getAction() {
      return action;
    }

    public String getRoles() {
      return roles;
    }
  }

  /**
   * Creates a label for the value cell of a row, if an <tt>IModel</tt> was provided instead of a <tt>Component</tt>.
   *
   * @param value the model to use for the value cell.
   * @return the label generated from the model, or null if the model contained a null value.
   */
  private Label createValueLabel(IModel<?> value) {
    // Wrap the original model that returns a whitespace when the original model value is null.
    // This allows the table cell tag to always have a body (ie: never renders <td/>).
    // A display issue (GFLX-111) was caused by this.
    // The reason we don't return a null component is because the original model may return a value later in our
    // life-cycle.

    // TODO: find a way to fix the display issue instead of hacking the models (ie: use css)
    return new Label(getRowValueId(), new Model<Serializable>(value) {

      private static final long serialVersionUID = 7669446358458768567L;

      @Override
      public Serializable getObject() {
        IModel<?> o = (IModel<?>) super.getObject();
        if(o.getObject() == null) return " ";
        return (Serializable) o.getObject();
      }

    });
  }

  private class KeyValueDataPanelHeaderFragment extends Fragment {

    private static final long serialVersionUID = -6785397649238353097L;

    private KeyValueDataPanelHeaderFragment(Component titleComponent) {
      super("header", "headerFragment", KeyValueDataPanel.this);
      if(titleComponent != null) {
        if(!titleComponent.getId().equals(getHeaderId())) {
          throw new IllegalArgumentException("KeyValueDataPanel header Component's id must be '" + getHeaderId() + "'");
        }
        add(titleComponent);
      } else {
        setVisible(false);
      }
    }

  }

  private class EmptyCellFragment extends Fragment {
    private static final long serialVersionUID = -1448002319546204879L;

    private EmptyCellFragment(String id) {
      super(id, "emptyCellFragment", KeyValueDataPanel.this);
    }

  }

}
