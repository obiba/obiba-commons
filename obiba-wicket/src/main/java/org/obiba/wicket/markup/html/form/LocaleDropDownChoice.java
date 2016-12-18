/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.wicket.markup.html.form;

import java.util.List;
import java.util.Locale;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;

/**
 * A {@code DropDownChoice} specialised for choosing a {@code Locale}. The {@code Locale}'s name is displayed using
 * its own locale instead of the session's locale. In other words, if the current session's locale is en and the locale
 * to display is fr, the generated label will be "français" (as opposed to "French"). To override this behaviour, use
 * the {@link #useSessionLocale} method by passing true.
 */
public class LocaleDropDownChoice extends DropDownChoice {

  private static final long serialVersionUID = -548446638790777870L;

  public LocaleDropDownChoice(String id) {
    super(id);
  }

  public LocaleDropDownChoice(String id, IModel model, List<Locale> choices) {
    super(id, model, choices, new LocaleChoiceRenderer());
  }

  public LocaleDropDownChoice(String id, IModel model, IModel choices) {
    super(id, model, choices, new LocaleChoiceRenderer());
  }

  /**
   * When true, the {@code Session}'s locale is used to display the choice's label, otherwise the label is generated
   * using its own locale.
   *
   * @param b
   */
  public void setUseSessionLocale(boolean b) {
    setChoiceRenderer(new LocaleChoiceRenderer(b));
  }

  public static class LocaleChoiceRenderer implements IChoiceRenderer {

    private static final long serialVersionUID = -4332854762230782314L;

    private boolean useSessionLocale = false;

    public LocaleChoiceRenderer() {

    }

    public LocaleChoiceRenderer(boolean useSessionLocale) {
      this.useSessionLocale = useSessionLocale;
    }

    @Override
    public Object getDisplayValue(Object object) {
      Locale lang = (Locale) object;
      Locale displayLocale = getDisplayLocale(lang);

      StringBuilder sb = new StringBuilder();
      sb.append(lang.getDisplayLanguage(displayLocale));
      if(lang.getCountry() != null && lang.getCountry().length() > 0) {
        sb.append(" (").append(lang.getDisplayCountry(displayLocale));
        if(lang.getVariant() != null && lang.getVariant().length() > 0) {
          sb.append(", ").append(lang.getDisplayVariant(displayLocale));
        }
        sb.append(")");
      }
      return sb.toString();
    }

    @Override
    public String getIdValue(Object object, int index) {
      Locale lang = (Locale) object;
      return lang.toString();
    }

    private Locale getDisplayLocale(Locale defaultLocale) {
      return useSessionLocale ? Session.get().getLocale() : defaultLocale;
    }
  }
}
