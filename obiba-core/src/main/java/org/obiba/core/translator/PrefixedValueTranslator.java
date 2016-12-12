package org.obiba.core.translator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrefixedValueTranslator implements Translator {

  private static final Pattern pattern = Pattern.compile("t\\(([^\\)]+)\\)");
  private Translator translator;

  public PrefixedValueTranslator(Translator translator) {
    this.translator = translator;
  }

  @Override
  public String translate(String prefixedValueToTranslate) {

    Matcher matcher = pattern.matcher(prefixedValueToTranslate);
    while (matcher.find()) {
      String word = matcher.group(1);
      String translatedWord = translator.translate(word);
      prefixedValueToTranslate = prefixedValueToTranslate.replace("t(" + word + ")", translatedWord);
    }

    return prefixedValueToTranslate;
  }
}
