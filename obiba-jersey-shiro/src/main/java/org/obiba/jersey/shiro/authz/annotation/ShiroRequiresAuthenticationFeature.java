package org.obiba.jersey.shiro.authz.annotation;

import java.io.IOException;
import java.lang.reflect.Method;

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
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.obiba.jersey.shiro.authz.annotation.CglibProxyUtils.isSuperMethodAnnotated;

@Provider
public class ShiroRequiresAuthenticationFeature implements DynamicFeature {

  private static final Logger log = LoggerFactory.getLogger(ShiroRequiresAuthenticationFeature.class);

  @Override
  public void configure(ResourceInfo resourceInfo, FeatureContext context) {
    Class<?> resourceClass = resourceInfo.getResourceClass();
    Method resourceMethod = resourceInfo.getResourceMethod();
    if(resourceClass.isAnnotationPresent(RequiresAuthentication.class) ||
        resourceClass.getSuperclass().isAnnotationPresent(RequiresAuthentication.class) ||
        resourceMethod.isAnnotationPresent(RequiresAuthentication.class) ||
        isSuperMethodAnnotated(resourceClass.getSuperclass(), resourceMethod, RequiresAuthentication.class)) {
      log.debug("Register RequiresAuthenticationRequestFilter for {}", resourceInfo);
      context.register(new RequiresAuthenticationRequestFilter());
    }
  }

  @Priority(Priorities.AUTHORIZATION) // authorization filter - should go after any authentication filters
  private static class RequiresAuthenticationRequestFilter implements ContainerRequestFilter {
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
      if(!SecurityUtils.getSubject().isAuthenticated()) {
        throw new ForbiddenException();
      }
    }
  }

}
