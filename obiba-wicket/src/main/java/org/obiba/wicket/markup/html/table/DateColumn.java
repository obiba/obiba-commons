package org.obiba.wicket.markup.html.table;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;


public class DateColumn extends PropertyColumn {

  private boolean withHour = false;
  
  private Locale locale;
  
  private String pattern = null;
  
  public DateColumn(IModel displayModel, String sortProperty, String propertyExpression, String pattern) {
    this(displayModel, sortProperty, propertyExpression, (Locale)null, false);
    this.pattern = pattern;
  }
  
  public DateColumn(IModel displayModel, String sortProperty, String propertyExpression, Locale locale, String pattern) {
    this(displayModel, sortProperty, propertyExpression, locale, false);
    this.pattern = pattern;
  }
  
  public DateColumn(IModel displayModel, String sortProperty, String propertyExpression, Locale locale) {
    this(displayModel, sortProperty, propertyExpression, locale, false);
  }
  
  public DateColumn(IModel displayModel, String sortProperty, String propertyExpression) {
    this(displayModel, sortProperty, propertyExpression, (Locale)null, false);
  }
  
  public DateColumn(IModel displayModel, String sortProperty, String propertyExpression, boolean withHour) {
    this(displayModel, sortProperty, propertyExpression, (Locale)null, withHour);
  }
  
  public DateColumn(IModel displayModel, String sortProperty, String propertyExpression, Locale locale, boolean withHour) {
    super(displayModel, sortProperty, propertyExpression);
    this.locale = locale;
    this.withHour = withHour;
  }

  public void populateItem(Item cellItem, String componentId, IModel rowModel) {
    PropertyModel pm = (PropertyModel)createLabelModel(rowModel);
    Object o = pm.getObject();
    if (o instanceof Date) {
      Date date = (Date)o;
      cellItem.add(new Label(componentId, getFormatedDate(date)));
    }
    else
      super.populateItem(cellItem, componentId, rowModel);
  }
  
  /**
   * Format given date, with(out) hour.
   * @param date
   * @param hour
   * @return
   */
  public String getFormatedDate(Date date) {
    if (date == null)
      return null;
    
    SimpleDateFormat formater;
  
    boolean resetPattern = false;
    if (pattern == null) {
      if (getLocale() != null && getLocale().getLanguage().equals(Locale.FRENCH.getLanguage())) {
        if (withHour)
          pattern = "yyyy-MM-dd HH:mm";
        else
          pattern = "yyyy-MM-dd";
      }
      else { // english style
        if (withHour)
          pattern = "MM/dd/yyyy HH:mm";
        else
          pattern = "MM/dd/yyyy";
      } 
      resetPattern = true;
    }
    
    if (getLocale() != null)
      formater = new SimpleDateFormat(pattern, getLocale());
    else
      formater = new SimpleDateFormat(pattern);

    if (resetPattern)
      pattern = null;
    
    return formater.format(date);
  }
  
  protected Locale getLocale() {
    return locale;
  }
  
}