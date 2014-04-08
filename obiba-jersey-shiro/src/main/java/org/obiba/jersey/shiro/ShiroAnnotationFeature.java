package org.obiba.jersey.shiro;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresGuest;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;

@Provider
public class ShiroAnnotationFeature implements DynamicFeature {

  @Override
  public void configure(ResourceInfo resourceInfo, FeatureContext context) {
    requiresAuthentication(resourceInfo, context);
    requiresRequiresGuest(resourceInfo, context);
    // TODO support @RequiresUser
    requiresPermissions(resourceInfo, context);
    requiresRoles(resourceInfo, context);
  }

  private void requiresAuthentication(ResourceInfo resourceInfo, FeatureContext context) {
    if(resourceInfo.getResourceClass().isAnnotationPresent(RequiresAuthentication.class) ||
        resourceInfo.getResourceMethod().isAnnotationPresent(RequiresAuthentication.class)) {
      context.register(new RequiresAuthenticationRequestFilter());
    }
  }

  private void requiresRequiresGuest(ResourceInfo resourceInfo, FeatureContext context) {
    if(resourceInfo.getResourceClass().isAnnotationPresent(RequiresGuest.class) ||
        resourceInfo.getResourceMethod().isAnnotationPresent(RequiresGuest.class)) {
      context.register(new RequiresGuestRequestFilter());
    }
  }

  private void requiresPermissions(ResourceInfo resourceInfo, FeatureContext context) {
    Collection<String> requiredPermissions = new ArrayList<>();
    if(resourceInfo.getResourceClass().isAnnotationPresent(RequiresPermissions.class)) {
      requiredPermissions
          .addAll(Arrays.asList(resourceInfo.getResourceClass().getAnnotation(RequiresPermissions.class).value()));
    }
    if(resourceInfo.getResourceMethod().isAnnotationPresent(RequiresPermissions.class)) {
      requiredPermissions
          .addAll(Arrays.asList(resourceInfo.getResourceMethod().getAnnotation(RequiresPermissions.class).value()));
    }
    if(!requiredPermissions.isEmpty()) {
      context.register(
          new RequiresPermissionsRequestFilter(requiredPermissions.toArray(new String[requiredPermissions.size()])));
    }
  }

  private void requiresRoles(ResourceInfo resourceInfo, FeatureContext context) {
    Collection<String> requiredRoles = new ArrayList<>();
    if(resourceInfo.getResourceClass().isAnnotationPresent(RequiresRoles.class)) {
      requiredRoles.addAll(Arrays.asList(resourceInfo.getResourceClass().getAnnotation(RequiresRoles.class).value()));
    }
    if(resourceInfo.getResourceMethod().isAnnotationPresent(RequiresRoles.class)) {
      requiredRoles.addAll(Arrays.asList(resourceInfo.getResourceMethod().getAnnotation(RequiresRoles.class).value()));
    }
    if(!requiredRoles.isEmpty()) {
      context.register(new RequiresRolesRequestFilter(requiredRoles));
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

  @Priority(Priorities.AUTHORIZATION) // authorization filter - should go after any authentication filters
  private static class RequiresGuestRequestFilter implements ContainerRequestFilter {
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
      if(SecurityUtils.getSubject().getPrincipal() != null) {
        throw new ForbiddenException();
      }
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
