package org.obiba.core.translator;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public class JsonTranslator implements Translator {

  private static final Logger logger = LoggerFactory.getLogger(JsonTranslator.class);

  private DocumentContext translationContext;

  public JsonTranslator(String translationsAsJson) {
    translationContext = JsonPath.parse(translationsAsJson);
  }

  @Override
  public String translate(String key) {
    try {
      return translationContext.read(key);
    } catch (JsonPathException e) {
      return key;
    }
  }

  public static Translator buildSafeTranslator(String translationsAsJson) {
    try {
      return new JsonTranslator(translationsAsJson);
    } catch (RuntimeException e) {
      logger.warn("Impossible to create json translator from this string. Create an empty translator instead. Given string : " + translationsAsJson);
      return new EmptyTranslator();
    }
  }

  public static Translator buildSafeTranslator(Callable<String> translationsProvider) {
    try {
      return buildSafeTranslator(translationsProvider.call());
    } catch (Exception e) {
      logger.warn("Impossible to get translations. Create an empty translator instead.", e);
      return new EmptyTranslator();
    }
  }
}
