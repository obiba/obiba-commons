/*
 *
 *  * Copyright (c) 2016 OBiBa. All rights reserved.
 *  *
 *  * This program and the accompanying materials
 *  * are made available under the terms of the GNU Public License v3.0.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.obiba.core.translator;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TranslationUtils {

  private static final Logger logger = LoggerFactory.getLogger(TranslationUtils.class);

  public String translate(String json, Translator translator) {
    if (json.startsWith("[")) {
      JSONArray jsonArray = new JSONArray(json);
      return this.translate(jsonArray, translator).toString();
    } else {
      JSONObject jsonObject = new JSONObject(json);
      return this.translate(jsonObject, translator).toString();
    }
  }

  public JSONObject translate(JSONObject object, Translator translator) {
    Iterator<String> keys = object.keys();
    while (keys.hasNext()) {
      String key = keys.next();
      Object property = object.get(key);
      logger.debug("{} : {}", key, property.toString());
      if (property instanceof String && !"".equals(property)) {
        object.put(key, translator.translate((String) property));
      } else if (property instanceof JSONArray) {
        translate((JSONArray) property, translator);
      } else if (property instanceof JSONObject) {
        translate((JSONObject) property, translator);
      }
    }
    return object;
  }

  private JSONArray translate(JSONArray array, Translator translator) {
    for (int i = 0; i < array.length(); i++) {
      Object value = array.get(i);
      if (value instanceof JSONArray) {
        translate((JSONArray) value, translator);
      } else if (value instanceof JSONObject) {
        translate((JSONObject) value, translator);
      } else if (value instanceof String) {
        array.put(i, translator.translate((String) value));
      }
    }
    return array;
  }
}
