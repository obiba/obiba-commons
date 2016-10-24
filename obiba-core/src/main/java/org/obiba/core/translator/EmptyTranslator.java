package org.obiba.core.translator;

public class EmptyTranslator implements Translator {

  @Override
  public String translate(String key) {
    return key;
  }
}
