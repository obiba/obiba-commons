/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.jersey.shiro.authz.annotation;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import jakarta.annotation.Priority;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.ext.Provider;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Arrays.asList;
import static org.obiba.jersey.shiro.authz.annotation.CglibProxyUtils.getSuperMethodAnnotation;
import static org.obiba.jersey.shiro.authz.annotation.CglibProxyUtils.isSuperMethodAnnotated;

@Provider
public class ShiroRequiresRolesFeature implements DynamicFeature {

  private static final Logger log = LoggerFactory.getLogger(ShiroRequiresRolesFeature.class);

  @Override
  public void configure(ResourceInfo resourceInfo, FeatureContext context) {
    Collection<String> requiredRoles = new ArrayList<>();
    Class<?> resourceClass = resourceInfo.getResourceClass();
    Method method = resourceInfo.getResourceMethod();

    if(resourceClass.isAnnotationPresent(RequiresRoles.class)) {
      requiredRoles.addAll(asList(resourceClass.getAnnotation(RequiresRoles.class).value()));
    }
    if(method.isAnnotationPresent(RequiresRoles.class)) {
      requiredRoles.addAll(asList(method.getAnnotation(RequiresRoles.class).value()));
    }

    // in case of Spring bean proxied by CGLIB (so without annotations)
    Class<?> superClass = resourceClass.getSuperclass();
    if(superClass.isAnnotationPresent(RequiresRoles.class)) {
      requiredRoles.addAll(asList(superClass.getAnnotation(RequiresRoles.class).value()));
    }
    if(isSuperMethodAnnotated(superClass, method, RequiresRoles.class)) {
      requiredRoles.addAll(asList(getSuperMethodAnnotation(superClass, method, RequiresRoles.class).value()));
    }

    if(!requiredRoles.isEmpty()) {
      log.debug("Register RequiresRolesRequestFilter for {} with {}", resourceInfo, requiredRoles);
      context.register(new RequiresRolesRequestFilter(requiredRoles));
    }
  }

  @Priority(Priorities.AUTHORIZATION) // authorization filter - should go after any authentication filters
  private static class RequiresRolesRequestFilter implements ContainerRequestFilter {

    private final Collection<String> requiredRoles;

    private RequiresRolesRequestFilter(Collection<String> requiredRoles) {
      this.requiredRoles = requiredRoles;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
      if(!SecurityUtils.getSubject().hasAllRoles(requiredRoles)) {
        throw new ForbiddenException();
      }
    }
  }

}
