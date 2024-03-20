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

import com.google.common.collect.Lists;
import com.google.protobuf.UninitializedMessageException;
import org.obiba.web.model.ErrorDtos;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
public class UninitializedMessageExceptionMapper
    extends AbstractErrorDtoExceptionMapper<UninitializedMessageException> {

  @Override
  protected Response.Status getStatus() {
    return Response.Status.BAD_REQUEST;
  }

  @Override
  protected ErrorDtos.ClientErrorDto getErrorDto(UninitializedMessageException exception) {
    return ErrorDtos.ClientErrorDto.newBuilder() //
        .setCode(getStatus().getStatusCode()) //
        .setMessageTemplate("error.uninitializedMessage") //
        .setMessage(exception.getMessage()) //
        .addAllArguments(Lists.newArrayList(exception.getMissingFields())) //
        .build();
  }

}
