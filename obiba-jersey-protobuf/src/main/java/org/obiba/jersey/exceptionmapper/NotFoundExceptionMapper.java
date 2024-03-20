package org.obiba.jersey.exceptionmapper;

import org.obiba.web.model.ErrorDtos;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
public class NotFoundExceptionMapper extends AbstractErrorDtoExceptionMapper<NotFoundException> {

  @Override
  protected Response.Status getStatus() {
    return Response.Status.NOT_FOUND;
  }

  @Override
  protected ErrorDtos.ClientErrorDto getErrorDto(NotFoundException e) {
    ErrorDtos.ClientErrorDto.Builder errorBuilder = ErrorDtos.ClientErrorDto.newBuilder()
        .setCode(getStatus().getStatusCode())
        .setMessageTemplate("server.error.not-found");

    if (e.getMessage() != null)
      errorBuilder.setMessage(e.getMessage());

    return errorBuilder.build();
  }

}
