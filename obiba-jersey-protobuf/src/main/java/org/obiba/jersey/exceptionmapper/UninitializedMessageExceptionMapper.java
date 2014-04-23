package org.obiba.jersey.exceptionmapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.obiba.web.model.ErrorDtos;

import com.google.common.collect.Lists;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.UninitializedMessageException;

@Provider
public class UninitializedMessageExceptionMapper
    extends AbstractErrorDtoExceptionMapper<UninitializedMessageException> {

  @Override
  protected Response.Status getStatus() {
    return Response.Status.BAD_REQUEST;
  }

  @Override
  protected GeneratedMessage.ExtendableMessage<?> getErrorDto(UninitializedMessageException exception) {
    return ErrorDtos.ClientErrorDto.newBuilder() //
        .setCode(getStatus().getStatusCode()) //
        .setMessageTemplate("error.uninitializedMessage") //
        .setMessage(exception.getMessage()) //
        .addAllArguments(Lists.newArrayList(exception.getMissingFields())) //
        .build();
  }

}
