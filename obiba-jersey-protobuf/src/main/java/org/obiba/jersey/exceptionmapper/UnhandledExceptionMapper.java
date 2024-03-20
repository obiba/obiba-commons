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

import org.obiba.web.model.ErrorDtos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

@Provider
public class UnhandledExceptionMapper extends AbstractErrorDtoExceptionMapper<Exception> {

  private static final Logger logger = LoggerFactory.getLogger(UnhandledExceptionMapper.class);

  @Override
  protected Response.Status getStatus() {
    return INTERNAL_SERVER_ERROR;
  }

  @Override
  protected ErrorDtos.ClientErrorDto getErrorDto(Exception exception) {

    logger.warn("Exception catched by UnhandledExceptionMapper", exception);

    ErrorDtos.ClientErrorDto.Builder errorBuilder = ErrorDtos.ClientErrorDto.newBuilder()
            .setCode(getStatus().getStatusCode())
            .setMessageTemplate("error.unhandledException");

    if (exception.getMessage() != null)
      errorBuilder.setMessage(exception.getMessage());

    return errorBuilder.build();
  }
}
