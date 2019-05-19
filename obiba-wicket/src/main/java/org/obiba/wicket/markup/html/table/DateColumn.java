/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.wicket.markup.html.table;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.annotation.Nullable;

import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

@SuppressWarnings("UnusedDeclaration")
public class DateColumn extends PropertyColumn {

  private boolean withHour = false;

  private final Locale locale;

  private String pattern = null;

  public DateColumn(IModel<String> displayModel, String sortProperty, String propertyExpression, String pattern) {
    this(displayModel, sortProperty, propertyExpression, null, false);
    this.pattern = pattern;
  }

  public DateColumn(IModel<String> displayModel, String sortProperty, String propertyExpression, Locale locale,
      String pattern) {
    this(displayModel, sortProperty, propertyExpression, locale, false);
    this.pattern = pattern;
  }

  public DateColumn(IModel<String> displayModel, String sortProperty, String propertyExpression, Locale locale) {
    this(displayModel, sortProperty, propertyExpression, locale, false);
  }

  public DateColumn(IModel<String> displayModel, String sortProperty, String propertyExpression) {
    this(displayModel, sortProperty, propertyExpression, null, false);
  }

  public DateColumn(IModel<String> displayModel, String sortProperty, String propertyExpression, boolean withHour) {
    this(displayModel, sortProperty, propertyExpression, null, withHour);
  }

  public DateColumn(IModel<String> displayModel, String sortProperty, String propertyExpression,
      @Nullable Locale locale, boolean withHour) {
    super(displayModel, sortProperty, propertyExpression);
    this.locale = locale;
    this.withHour = withHour;
  }

  @Override
  public void populateItem(Item cellItem, String componentId, IModel rowModel) {
    PropertyModel pm = (PropertyModel) createLabelModel(rowModel);
    Object o = pm.getObject();
    if(o instanceof Date) {
      Date date = (Date) o;
      cellItem.add(new Label(componentId, getFormatedDate(date)));
    } else {
      super.populateItem(cellItem, componentId, rowModel);
    }
  }

  /**
   * Format given date, with(out) hour.
   *
   * @param date
   * @param hour
   * @return
   */
  @Nullable
  public String getFormatedDate(Date date) {
    if(date == null) return null;

    boolean resetPattern = false;
    if(pattern == null) {
      if(getLocale() != null && getLocale().getLanguage().equals(Locale.FRENCH.getLanguage())) {
        pattern = withHour ? "yyyy-MM-dd HH:mm" : "yyyy-MM-dd";
      } else { // english style
        pattern = withHour ? "MM/dd/yyyy HH:mm" : "MM/dd/yyyy";
      }
      resetPattern = true;
    }

    SimpleDateFormat formatter = getLocale() == null
        ? new SimpleDateFormat(pattern)
        : new SimpleDateFormat(pattern, getLocale());

    if(resetPattern) pattern = null;

    return formatter.format(date);
  }

  @Nullable
  protected Locale getLocale() {
    return locale;
  }

}