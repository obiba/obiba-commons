/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.apache.shiro.realm.crowd;

/**
 * The Atlassian Crowd server as the concept of role and group memberships.
 * Both of which can be can be mapped to Shiro roles.
 * This realm implementation allows the deployer to select either or both memberships to map to Shiro roles.
 * <p>
 * These enums are use to direct the Shiro realm where to obtain roles.
 * Either or both of the enums may be used.
 * </p>
 *
 * @version $Rev: 1023292 $ $Date: 2010-10-16 07:31:35 -0700 (Sat, 16 Oct 2010) $
 * @see <a href="https://code.google.com/a/apache-extras.org/p/atlassian-crowd-realm">https://code.google.com/a/apache-extras.org/p/atlassian-crowd-realm</a>
 */
public enum RoleSource {

  /**
   * Obtain Shiro roles from Crowd group memberships
   */
  ROLES_FROM_CROWD_GROUPS,

  /**
   * Obtain Shiro roles from Crowd role memberships
   */
  ROLES_FROM_CROWD_ROLES
}
