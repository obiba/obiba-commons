/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.jersey.exceptionmapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.GeneratedMessage;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

public abstract class AbstractErrorDtoExceptionMapper<TException extends Throwable>
    implements ExceptionMapper<TException> {

  protected final Logger log = LoggerFactory.getLogger(getClass());

  protected abstract Response.Status getStatus();

  protected abstract GeneratedMessage.ExtendableMessage<?> getErrorDto(TException exception);

  @Override
  public Response toResponse(TException exception) {
    log.debug("{}", exception.getClass().getSimpleName(), exception);
    GeneratedMessage.ExtendableMessage<?> errorDto = getErrorDto(exception);
    log.debug("ErrorDto: {}", errorDto);
    //TODO support also application/x-protobuf
    return Response.status(getStatus()).type(APPLICATION_JSON).entity(errorDto).build();
  }

}
