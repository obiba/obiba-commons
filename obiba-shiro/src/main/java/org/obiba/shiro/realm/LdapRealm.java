/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.shiro.realm;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.AuthenticationException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.ldap.JndiLdapRealm;
import org.apache.shiro.realm.ldap.LdapContextFactory;
import org.apache.shiro.realm.ldap.LdapUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LDAP {@link JndiLdapRealm} implementation that supports authorization.
 * <p>
 * Here is a sample config for shiro.ini for a basic OpenLDAP config:
 * <pre>
 *   # LDAP realm configuration
 *   ldapRealm = org.obiba.security.realm.LdapRealm
 *   ldapRealm.userDnTemplate = uid={0},ou=people,dc=example,dc=com
 *   ldapRealm.contextFactory.url = ldap://localhost
 *   ldapRealm.contextFactory.authenticationMechanism = none
 *   ldapRealm.contextFactory.systemUsername = admin
 *   ldapRealm.contextFactory.systemPassword = secret
 *   ldapRealm.searchBase = dc=example,dc=com
 *   ldapRealm.userGroupAttribute = memberUid
 *   ldapRealm.groupNameAttribute = cn
 *   # Specify mapping between LDAP groups and your application roles
 *   ldapRealm.groupRolesMap = group1:SYSTEM_ADMINISTRATOR, group2:PARTICIPANT_MANAGER, group3:DATA_COLLECTION_OPERATOR
 * </pre>
 * </p>
 */
@SuppressWarnings("UnusedDeclaration")
public class LdapRealm extends JndiLdapRealm {

  private final static Logger logger = LoggerFactory.getLogger(LdapRealm.class);

  private String searchBase;

  private String userGroupAttribute;

  private String groupNameAttribute;

  private Map<String, String> groupRolesMap;

  /**
   * Get groups from LDAP.
   *
   * @param principals the principals of the Subject whose AuthenticationInfo should be queried from the LDAP server.
   * @param ldapContextFactory factory used to retrieve LDAP connections.
   * @return an {@link AuthorizationInfo} instance containing information retrieved from the LDAP server.
   * @throws NamingException if any LDAP errors occur during the search.
   */
  @Override
  protected AuthorizationInfo queryForAuthorizationInfo(PrincipalCollection principals,
      LdapContextFactory ldapContextFactory) throws NamingException {

    Set<String> roleNames = new HashSet<String>();
    String username = (String) getAvailablePrincipal(principals);

    LdapContext systemLdapCtx = null;
    try {
      systemLdapCtx = ldapContextFactory.getSystemLdapContext();

      SearchControls constraints = new SearchControls();
      constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);

      NamingEnumeration<?> answer = systemLdapCtx.search(searchBase, userGroupAttribute + "=" + username, constraints);
      while(answer.hasMore()) {
        queryResult(roleNames, (SearchResult) answer.next());
      }

    } catch(AuthenticationException e) {
      // do nothing as the principal was not authenticated on LDAP
    } finally {
      LdapUtils.closeContext(systemLdapCtx);
    }

    logger.debug("Role for {}: {}", username, roleNames);

    return new SimpleAuthorizationInfo(roleNames);
  }

  private void queryResult(Collection<String> roleNames, SearchResult sr) throws NamingException {
    for(NamingEnumeration<?> attributesEnum = sr.getAttributes().getAll(); attributesEnum.hasMore(); ) {
      Attribute attr = (Attribute) attributesEnum.next();
      if(attr.getID().equalsIgnoreCase(groupNameAttribute)) {
        NamingEnumeration<?> e = attr.getAll();
        while(e.hasMore()) {
          String role = groupRolesMap.get(e.next());
          if(role != null) roleNames.add(role);
        }
      }
    }
  }

  public void setSearchBase(String searchBase) {
    this.searchBase = searchBase;
  }

  public void setUserGroupAttribute(String userGroupAttribute) {
    this.userGroupAttribute = userGroupAttribute;
  }

  public void setGroupNameAttribute(String groupNameAttribute) {
    this.groupNameAttribute = groupNameAttribute;
  }

  /**
   * Set mapping between LDAP groups and application roles
   *
   * @param groupRolesMap
   */
  public void setGroupRolesMap(Map<String, String> groupRolesMap) {
    this.groupRolesMap = groupRolesMap;
  }
}
