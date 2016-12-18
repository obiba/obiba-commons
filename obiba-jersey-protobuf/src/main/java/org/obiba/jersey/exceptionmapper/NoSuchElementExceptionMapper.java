/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.jersey.exceptionmapper;

import java.util.NoSuchElementException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class NoSuchElementExceptionMapper implements ExceptionMapper<NoSuchElementException> {

  private static final Logger log = LoggerFactory.getLogger(NoSuchElementExceptionMapper.class);

  @Override
  public Response toResponse(NoSuchElementException exception) {
    log.debug("{}", exception.getClass().getSimpleName(), exception);
    return Response.status(Status.NOT_FOUND).entity(exception.getMessage()).build();
  }

}
