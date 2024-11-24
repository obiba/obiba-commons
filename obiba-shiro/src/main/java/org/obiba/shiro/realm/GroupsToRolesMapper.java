package org.obiba.shiro.realm;

import java.util.Set;

/**
 * Convert groups to roles. Default is direct mapping.
 */
public interface GroupsToRolesMapper {

  default Set<String> toRoles(Set<String> groups) {
    return groups;
  }

}
