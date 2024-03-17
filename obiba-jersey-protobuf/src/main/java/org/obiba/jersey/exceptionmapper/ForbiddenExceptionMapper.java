package org.obiba.jersey.exceptionmapper;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.obiba.web.model.ErrorDtos;

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
