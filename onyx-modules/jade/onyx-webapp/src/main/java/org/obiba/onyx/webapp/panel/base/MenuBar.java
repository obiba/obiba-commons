package org.obiba.onyx.webapp.panel.base;

import java.util.Arrays;
import java.util.Locale;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.webapp.page.base.BasePage;

/**
 * Base page menu bar.
 * @author ymarcon
 * 
 */
public class MenuBar extends Panel {

  private static final long serialVersionUID = 1L;

  @SpringBean
  private EntityQueryService queryService;

  public MenuBar(String id) {
    super(id);

    MenuBuilder.build(this);
    // language selection
    AjaxLanguageChoicePanel languageSelect = new AjaxLanguageChoicePanel("languageSelect", new StringResourceModel("language", this, null), Arrays.asList(new Locale[] { Locale.FRENCH, Locale.ENGLISH })) {

      private static final long serialVersionUID = 1L;

      @Override
      protected void onLanguageUpdate(Locale language, AjaxRequestTarget target) {
        if(language == null) return;

        getSession().setLocale(language);
        if(!(getPage() instanceof BasePage)) setResponsePage(getPage());
        else
          ((BasePage) getPage()).onLanguageUpdate(language, target);
      }

    };
    if(getSession().getLocale() != null && getSession().getLocale().getLanguage().equals("fr")) getSession().setLocale(Locale.FRENCH);
    else
      getSession().setLocale(Locale.ENGLISH);

    languageSelect.setSelectedLanguage(getSession().getLocale());
    add(languageSelect);
  }

}