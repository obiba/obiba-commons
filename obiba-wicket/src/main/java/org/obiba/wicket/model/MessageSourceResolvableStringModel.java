package org.obiba.wicket.model;

import java.io.Serializable;
import java.util.Locale;

import org.apache.wicket.Application;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebSession;
import org.obiba.wicket.application.ISpringWebApplication;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;

/**
 * A model that represents a localized string obtained through Spring's l10n mechanism. Given a
 * {@code MessageSourceResolvable} this model will return the associated string using a call to
 * {@code MessageSource#getMessage(MessageSourceResolvable, Locale)}.
 * <p/>
 * When not specified, the {@code MessageSource} instance defaults to the {@code ApplicationContext} which is obtained
 * through the {@code ISpringContextLocator}.
 * <p/>
 * When not specified, the locale model uses the current {@code WebSession}'s locale.
 */
public class MessageSourceResolvableStringModel extends AbstractReadOnlyModel {

  private static final long serialVersionUID = 1936149729794048090L;

  private MessageSource messageSource;

  private IModel localeModel;

  private IModel messageSourceResolvableModel;

  public MessageSourceResolvableStringModel(MessageSource messageSource, IModel messageSourceResolvableModel,
      IModel localeModel) {
    this.messageSource = messageSource;
    this.messageSourceResolvableModel = messageSourceResolvableModel;
    this.localeModel = localeModel;

    if(this.messageSourceResolvableModel == null)
      throw new IllegalArgumentException("MessageSourceResolvableModel cannot be null");
  }

  public MessageSourceResolvableStringModel(MessageSource messageSource, IModel messageSourceResolvableModel) {
    this(messageSource, messageSourceResolvableModel, null);
  }

  public MessageSourceResolvableStringModel(IModel messageSourceResolvableModel) {
    this(null, messageSourceResolvableModel);
  }

  /**
   * @param messageSourceResolvable must also implement {@code java.io.Serializable}.
   */
  public MessageSourceResolvableStringModel(MessageSourceResolvable messageSourceResolvable) {
    this(new Model((Serializable) messageSourceResolvable));
  }

  @Override
  public void detach() {
    if(localeModel != null) {
      this.localeModel.detach();
    }
    this.messageSourceResolvableModel.detach();
    super.detach();
  }

  @Override
  public Object getObject() {
    return getMessageSource()
        .getMessage((MessageSourceResolvable) messageSourceResolvableModel.getObject(), getLocale());
  }

  private Locale getLocale() {
    if(localeModel != null) {
      return (Locale) localeModel.getObject();
    }
    return WebSession.get().getLocale();
  }

  private MessageSource getMessageSource() {
    if(messageSource != null) {
      return messageSource;
    }
    Application application = Application.get();
    if(application instanceof ISpringWebApplication) {
      return ((ISpringWebApplication) application).getSpringContextLocator().getSpringContext();
    }

    throw new IllegalStateException(
        "Cannot find MessageSource. Application must either implement ISpringWebApplication or extend SpringWebApplication.");
  }

}
