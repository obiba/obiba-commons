/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.jersey.exceptionmapper;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.obiba.web.model.ErrorDtos;

import com.google.protobuf.GeneratedMessage;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Provider
public class ConstraintViolationExceptionMapper extends AbstractErrorDtoExceptionMapper<ConstraintViolationException> {

  @Override
  protected Response.Status getStatus() {
    return BAD_REQUEST;
  }

  @Override
  protected GeneratedMessage.ExtendableMessage<?> getErrorDto(ConstraintViolationException exception) {

    ErrorDtos.ClientErrorDto.Builder builder = ErrorDtos.ClientErrorDto.newBuilder() //
        .setCode(getStatus().getStatusCode()) //
        .setMessageTemplate("error.constraintViolation");
    if(exception.getMessage() != null) {
      builder.setMessage(exception.getMessage());
    }

    for(ConstraintViolation<?> violation : exception.getConstraintViolations()) {
      String trimmedMessageTemplate = violation.getMessageTemplate()
          .substring(1, violation.getMessageTemplate().length() - 1);
      builder.addExtension(ErrorDtos.ConstraintViolationErrorDto.errors,
          ErrorDtos.ConstraintViolationErrorDto.newBuilder() //
              .setMessage(violation.getMessage()) //
              .setMessageTemplate(trimmedMessageTemplate) //
              .setPropertyPath(violation.getPropertyPath().toString()).build()
      );
    }

    return builder.build();
  }

}
