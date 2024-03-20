package org.obiba.jersey.exceptionmapper;

import org.obiba.web.model.ErrorDtos;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
public class ForbiddenExceptionMapper extends AbstractErrorDtoExceptionMapper<ForbiddenException> {

  @Override
  protected Response.Status getStatus() {
    return Response.Status.FORBIDDEN;
  }

  @Override
  protected ErrorDtos.ClientErrorDto getErrorDto(ForbiddenException e) {
    ErrorDtos.ClientErrorDto.Builder errorBuilder = ErrorDtos.ClientErrorDto.newBuilder()
        .setCode(getStatus().getStatusCode())
        .setMessageTemplate("server.error.forbidden");

    if (e.getMessage() != null)
      errorBuilder.setMessage(e.getMessage());

    return errorBuilder.build();
  }
}
