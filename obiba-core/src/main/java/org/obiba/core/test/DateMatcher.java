package org.obiba.core.test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

/**
 * Implementation of {@link Matcher} for comparing dates and time.
 */
public class DateMatcher extends BaseMatcher<Date> {

  /**
   * The expected value
   */
  private final Calendar value;

  /**
   * An array of Calendar fields that should be compared
   */
  private final int[] fields;

  /**
   * A description of the matching
   */
  private final String desc;

  private DateMatcher(Date d, String desc, int[] fields) {
    (value = GregorianCalendar.getInstance()).setTime(d);
    this.fields = fields;
    this.desc = desc;
  }

  @Override
  public void describeTo(Description description) {
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd G 'at' HH:mm:ss z");
    description.appendText("the same ").appendText(desc).appendText(" as ").appendValue(df.format(value.getTime()));
  }

  @Override
  public boolean matches(Object obj) {
    if(obj instanceof Date) {
      Calendar c = GregorianCalendar.getInstance();
      c.setTime((Date) obj);
      for(int i = 0; i < fields.length; i++) {
        int field = fields[i];
        if(value.get(field) != c.get(field)) return false;
      }
      return true;
    }
    return false;
  }

  static public DateMatcher same(Date d, String desc, int... fields) {
    return new DateMatcher(d, desc, fields);
  }

  /**
   * Matches the year and the day of year of two dates.
   *
   * @param d
   * @return
   */
  static public DateMatcher sameDate(Date d) {
    return same(d, "date", Calendar.YEAR, Calendar.DAY_OF_YEAR);
  }

  /**
   * Matches the date and time (up to the seconds).
   *
   * @param d
   * @return
   */
  static public DateMatcher sameDateAndTime(Date d) {
    return same(d, "date and time", Calendar.YEAR, Calendar.DAY_OF_YEAR, Calendar.HOUR_OF_DAY, Calendar.MINUTE,
        Calendar.SECOND);
  }

  /**
   * Matches the day of the year, ie: January first.
   *
   * @param d
   * @return
   */
  static public DateMatcher sameDayInYear(Date d) {
    return same(d, "day of year", Calendar.DAY_OF_YEAR);
  }

  /**
   * Matches the time of day up to the seconds.
   *
   * @param d
   * @return
   */
  static public DateMatcher sameTimeOfDay(Date d) {
    return same(d, "time of day", Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND);
  }

}