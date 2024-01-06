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

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;
import com.google.protobuf.util.JsonFormat;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Provider
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class ProtobufJsonProvider extends AbstractProtobufProvider
    implements MessageBodyReader<Object>, MessageBodyWriter<Object> {

  private static final Logger log = LoggerFactory.getLogger(ProtobufJsonProvider.class);

  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return Message.class.isAssignableFrom(type) || isWrapped(type, genericType);
  }

  @Override
  public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
      throws IOException, WebApplicationException {
    Class<Message> messageType = extractMessageType(type, genericType);

    ExtensionRegistry extensionRegistry = extensions().forMessage(messageType);
    Builder builder = builders().forMessage(messageType);

    Reader input = new InputStreamReader(entityStream, Charsets.UTF_8);
    // FIXME is there a pbf support for arrays?
    // FIXME how do we pass the extensionRegistry?
    JsonFormat.parser().merge(input, builder);
    return builder.build();
  }

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return Message.class.isAssignableFrom(type) || isWrapped(type, genericType);
  }

  @Override
  public long getSize(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return -1;
  }

  @Override
  @SuppressWarnings({ "unchecked", "PMD.ExcessiveParameterList" })
  public void writeTo(Object obj, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
      throws IOException, WebApplicationException {

    try(OutputStreamWriter output = new OutputStreamWriter(entityStream, Charsets.UTF_8)) {
      if(isWrapped(type, genericType)) {
        // JsonFormat does not provide a printList method
        if(log.isDebugEnabled()) {
          Appendable sb = new StringBuilder();
          JsonIoUtil.printCollection((Iterable<Message>) obj, sb);
          log.trace("Print message collection: {}", sb);
        }
        JsonIoUtil.printCollection((Iterable<Message>) obj, output);
      } else {
        log.trace("Print single message: {}", JsonFormat.printer().print((Message) obj));
        JsonFormat.printer().appendTo((Message) obj, output);
      }
      output.flush();
    }
  }
}
