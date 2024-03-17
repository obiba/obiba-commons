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

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.Provider;
import org.obiba.web.model.ErrorDtos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;

@Provider
public class NotFoundExceptionMapper extends AbstractErrorDtoExceptionMapper<NotFoundException> {

  @Override
  protected Status getStatus() {
    return Status.NOT_FOUND;
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
