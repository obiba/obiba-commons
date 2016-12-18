/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.jersey.protobuf;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;
import com.googlecode.protobuf.format.JsonFormat;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

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

    Readable input = new InputStreamReader(entityStream, Charsets.UTF_8);
    if(isWrapped(type, genericType)) {
      // JsonFormat does not provide a mergeCollection method
      return JsonIoUtil.mergeCollection(input, extensionRegistry, builder);
    }
    JsonFormat.merge(input, extensionRegistry, builder);
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
        log.trace("Print single message: {}", JsonFormat.printToString((Message) obj));
        JsonFormat.print((Message) obj, output);
      }
      output.flush();
    }
  }
}
