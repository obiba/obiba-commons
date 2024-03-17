package org.obiba.jersey.exceptionmapper;

import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.obiba.web.model.ErrorDtos;

@Provider
public class NotAuthorizedExceptionMapper extends AbstractErrorDtoExceptionMapper<NotAuthorizedException> {

  @Override
  protected Response.Status getStatus() {
    return Response.Status.UNAUTHORIZED;
  }

  @Override
  protected ErrorDtos.ClientErrorDto getErrorDto(NotAuthorizedException e) {
    ErrorDtos.ClientErrorDto.Builder errorBuilder = ErrorDtos.ClientErrorDto.newBuilder()
        .setCode(getStatus().getStatusCode())
        .setMessageTemplate("server.error.not-authorized");

    if (e.getMessage() != null)
      errorBuilder.setMessage(e.getMessage());

    return errorBuilder.build();
  }
}
