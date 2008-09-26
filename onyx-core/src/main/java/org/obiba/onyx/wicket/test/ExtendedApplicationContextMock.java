package org.obiba.onyx.wicket.test;

import java.util.Locale;

import org.apache.wicket.spring.test.ApplicationContextMock;

public class ExtendedApplicationContextMock extends ApplicationContextMock {

  private static final long serialVersionUID = 1L;

  private String message;
  
  /**
   * Returns a "canned" message -- the message specified by the last call 
   * to <code>setMessage</code>.
   * 
   * NOTE: All arguments are ignored. The same "canned" message is returned
   * in all cases.
   * 
   * @param code message key
   * @param args message arguments (for placeholders)
   * @param locale message locale
   * @return message previously set by <code>setMessage</code>
   */
  public String getMessage(String code, Object[] args, Locale locale) {
    return message;
  }
  
  /**
   * Sets the message returned by <code>getMessage</code>.
   * 
   * @param message the message
   */ 
  public void setMessage(String message) {
    this.message = message;
  }
}