package org.obiba.jersey.exceptionmapper;

import org.obiba.web.model.ErrorDtos;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

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
