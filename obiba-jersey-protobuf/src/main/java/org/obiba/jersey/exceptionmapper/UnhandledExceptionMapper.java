/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.jersey.exceptionmapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.obiba.web.model.ErrorDtos;

import com.google.protobuf.GeneratedMessage;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

@Provider
public class UnhandledExceptionMapper extends AbstractErrorDtoExceptionMapper<Exception> {

  @Override
  protected Response.Status getStatus() {
    return INTERNAL_SERVER_ERROR;
  }

  @Override
  protected GeneratedMessage.ExtendableMessage<?> getErrorDto(Exception exception) {
    return ErrorDtos.ClientErrorDto.newBuilder() //
        .setCode(getStatus().getStatusCode()) //
        .setMessageTemplate("error.unhandledException") //
        .setMessage(exception.getMessage()) //
        .build();
  }

}
