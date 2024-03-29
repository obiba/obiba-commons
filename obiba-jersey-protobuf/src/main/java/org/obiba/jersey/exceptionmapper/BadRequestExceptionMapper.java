package org.obiba.jersey.exceptionmapper;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.obiba.web.model.ErrorDtos;

@Provider
public class BadRequestExceptionMapper extends AbstractErrorDtoExceptionMapper<BadRequestException> {

  @Override
  protected Response.Status getStatus() {
    return Response.Status.BAD_REQUEST;
  }

  @Override
  protected ErrorDtos.ClientErrorDto getErrorDto(BadRequestException e) {
    ErrorDtos.ClientErrorDto.Builder errorBuilder = ErrorDtos.ClientErrorDto.newBuilder()
        .setCode(getStatus().getStatusCode())
        .setMessageTemplate("server.error.bad-request");

    if (e.getMessage() != null)
      errorBuilder.setMessage(e.getMessage());

    return errorBuilder.build();
  }
}
