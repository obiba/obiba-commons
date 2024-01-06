/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.jersey.protobuf;

import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;

import java.io.IOException;

/**
 * Utility class that provides a simple way of writing collections of messages as a JSON array. This method will
 * delegate the Message writing to {@code JsonFormat}.
 */
@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
public final class JsonIoUtil {

  private static final char JS_ARRAY_OPEN = '[';

  private static final char JS_ARRAY_SEP = ',';

  private static final char JS_ARRAY_CLOSE = ']';

  private JsonIoUtil() {
  }

  public static void printCollection(Iterable<? extends Message> messages, Appendable appendable) throws IOException {
    if(messages == null) throw new IllegalArgumentException("messages cannot be null");
    if(appendable == null) throw new IllegalArgumentException("messages cannot be null");

    // Start the Array
    appendable.append(JS_ARRAY_OPEN);
    boolean first = true;
    for(Message ml : messages) {
      // If this isn't the first item, prepend with a comma
      if(!first) appendable.append(JS_ARRAY_SEP);
      first = false;

      JsonFormat.printer().appendTo(ml, appendable);
    }
    // Close the Array
    appendable.append(JS_ARRAY_CLOSE);
  }

}
