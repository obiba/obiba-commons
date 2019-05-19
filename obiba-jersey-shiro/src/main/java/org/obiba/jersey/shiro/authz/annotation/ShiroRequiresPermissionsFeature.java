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

import javax.annotation.Priority;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Arrays.asList;
import static org.obiba.jersey.shiro.authz.annotation.CglibProxyUtils.getSuperMethodAnnotation;
import static org.obiba.jersey.shiro.authz.annotation.CglibProxyUtils.isSuperMethodAnnotated;

@Provider
public class ShiroRequiresPermissionsFeature implements DynamicFeature {

  private static final Logger log = LoggerFactory.getLogger(ShiroRequiresPermissionsFeature.class);

  @Override
  public void configure(ResourceInfo resourceInfo, FeatureContext context) {
    Collection<String> requiredPermissions = new ArrayList<>();
    Class<?> resourceClass = resourceInfo.getResourceClass();
    Method method = resourceInfo.getResourceMethod();

    if(resourceClass.isAnnotationPresent(RequiresPermissions.class)) {
      requiredPermissions.addAll(asList(resourceClass.getAnnotation(RequiresPermissions.class).value()));
    }
    if(method.isAnnotationPresent(RequiresPermissions.class)) {
      requiredPermissions.addAll(asList(method.getAnnotation(RequiresPermissions.class).value()));
    }

    // in case of Spring bean proxied by CGLIB (where we cannot access annotations anymore)
    Class<?> superClass = resourceClass.getSuperclass();
    if(superClass.isAnnotationPresent(RequiresPermissions.class)) {
      requiredPermissions.addAll(asList(superClass.getAnnotation(RequiresPermissions.class).value()));
    }
    if(isSuperMethodAnnotated(superClass, method, RequiresPermissions.class)) {
      requiredPermissions
          .addAll(asList(getSuperMethodAnnotation(superClass, method, RequiresPermissions.class).value()));
    }

    if(!requiredPermissions.isEmpty()) {
      log.debug("Register RequiresPermissionsRequestFilter for {} with {}", resourceInfo, requiredPermissions);
      context.register(
          new RequiresPermissionsRequestFilter(requiredPermissions.toArray(new String[requiredPermissions.size()])));
    }
  }

  @Priority(Priorities.AUTHORIZATION) // authorization filter - should go after any authentication filters
  private static class RequiresPermissionsRequestFilter implements ContainerRequestFilter {

    private final String[] requiredPermissions;

    private RequiresPermissionsRequestFilter(String... requiredPermissions) {
      this.requiredPermissions = requiredPermissions;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
      if(!SecurityUtils.getSubject().isPermittedAll(requiredPermissions)) {
        throw new ForbiddenException();
      }
    }
  }

}
