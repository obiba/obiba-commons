package org.obiba.wicket.markup.html.panel;

import java.util.HashMap;
import java.util.Map;

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
import org.apache.wicket.model.StringResourceModel;

/**
 * A convenient class for generating tables of data.
 * 
 * @author ymarcon
 * 
 */
public class KeyValueDataPanel extends Panel {

  private static final long serialVersionUID = 5705222737293170400L;

  Map<Component, Component> rowMap = new HashMap<Component, Component>();

  RepeatingView view;

  // List<Row> rows = new LinkedList<Row>();
  ListView myListView;

  int rowCounter = 0;

  private WebMarkupContainer noDataRow = new WebMarkupContainer("noDataRow");

  public KeyValueDataPanel(String id) {
    this(id, (Component) null);
  }

  /**
   * 
   * @param id
   * @param header
   *          use {@link #getHeaderId()} to put the right id to the component
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

  public KeyValueDataPanel(String id, IModel header) {
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
  public void setHeader(IModel header) {
    setHeader(new Label(getHeaderId(), header));
  }

  /**
   * Set/replace the current header.
   * 
   * @param header
   */
  public void setHeader(Component header) {
    KeyValueDataPanelHeaderFragment newHeader = new KeyValueDataPanelHeaderFragment(header);
    if (get("header") != null)
      get("header").replaceWith(newHeader);
    else
      add(newHeader);
  }

  public void showNoDataRow(boolean show) {
    noDataRow.setVisible(show);
  }

  /**
   * Add labels, with autorization filter.
   * 
   * @param pKey
   * @param pValue
   * @param pRowAuth
   */
  public void addRow(IModel pKey, IModel pValue, RowAuthorization... pRowAuth) {
    addRow(pKey, pValue, false, pRowAuth);
  }

  /**
   * Add labels, with key indentation and autorization filter.
   * 
   * @param pKey
   * @param pValue
   * @param indent
   * @param pRowAuth
   */
  public void addRow(IModel pKey, IModel pValue, boolean indent, RowAuthorization... pRowAuth) {
    addRow(new Label(getRowKeyId(), pKey), createValueLabel(pValue), indent, pRowAuth);
  }

  /**
   * Add components row, with autorization filter.
   * 
   * @param pKey
   * @param pValue
   * @param pRowAuth
   */
  public void addRow(IModel pKey, Component pValue, RowAuthorization... pRowAuth) {
    addRow(pKey, pValue, false, pRowAuth);
  }

  /**
   * Add components row, with key indentation and autorization filter.
   * 
   * @param pKey
   * @param pValue
   * @param indent
   * @param pRowAuth
   */
  public void addRow(IModel pKey, Component pValue, boolean indent, RowAuthorization... pRowAuth) {
    addRow(new Label(getRowKeyId(), pKey), pValue, indent, pRowAuth);
  }

  /**
   * Add components row, with autorization filter.
   * 
   * @param pKey
   * @param pValue
   * @param pRowAuth
   */
  public void addRow(Component pKey, Component pValue, RowAuthorization... pRowAuth) {
    addRow(pKey, pValue, false, pRowAuth);
  }
  
  
  /**
   * Creates a label for the value cell of a row, if an <tt>IModel</tt> was provided instead of a <tt>Component</tt>.
   * @param pValue the model to use for the value cell.
   * @return the label generated from the model, or null if the model contained a null value.
   */
  private Label createValueLabel(IModel pValue) {
    Label l = null;
    if (pValue.getObject() != null) {
      l = new Label(getRowValueId(), pValue);
    }
    return l;
  }
  

  /**
   * Add components row, with autorization filter.
   * 
   * @param pKey
   * @param pValue
   * @param pRowAuth
   */
  public void addRow(Component pKey, Component pValue, final boolean indent, RowAuthorization... pRowAuth) {
    rowCounter++;
    WebMarkupContainer item = new WebMarkupContainer(view.newChildId());
    view.add(item);

    item.add(pKey);

    pKey.add(new AttributeModifier("class", true, new AbstractReadOnlyModel() {
      private static final long serialVersionUID = 825034630638663648L;

      public Object getObject() {
        return (indent) ? getKeyIndentCssClass() : getKeyCssClass();
      }
    }));

    // If the value's is empty, generate an empty component with a spacing character.
    // This prevents some display glitches on the table.
    if (pValue == null) {
      pValue = new EmptyCellFragment(getRowValueId());
    }

    item.add(pValue);

    if (pRowAuth != null) {
      for (int i = 0; i < pRowAuth.length; i++) {
        MetaDataRoleAuthorizationStrategy.authorize(item, pRowAuth[i].getAction(), pRowAuth[i].getRoles());
      }
    }

    pValue.add(new AttributeModifier("class", true, new AbstractReadOnlyModel() {
      private static final long serialVersionUID = 8250197630638663648L;

      int rowIndex = rowCounter;

      public Object getObject() {
        return (rowIndex % 2 == 1) ? getValueOddCssClass() : getValueEvenCssClass();
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
    private Action action;

    private String roles;

    public RowAuthorization(Action pAction, String pRoles) {
      action = pAction;
      roles = pRoles;
    }

    public Action getAction() {
      return action;
    }

    public String getRoles() {
      return roles;
    }
  }

  private class KeyValueDataPanelHeaderFragment extends Fragment {

    private static final long serialVersionUID = -6785397649238353097L;

    public KeyValueDataPanelHeaderFragment(Component titleComponent) {
      super("header", "headerFragment", KeyValueDataPanel.this);
      if (titleComponent != null) {
        if (titleComponent.getId().equals(getHeaderId()) == false) {
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

    public EmptyCellFragment(String id) {
      super(id, "emptyCellFragment", KeyValueDataPanel.this);
    }

  }

  private class NoDataRowFragment extends Fragment {
    public NoDataRowFragment(String id) {
      super(id, "noDataRow", KeyValueDataPanel.this);
    }
  }

}
