/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.shiro.realm.crowd;

import java.rmi.RemoteException;
import java.util.EnumSet;
import java.util.Map;

import com.atlassian.crowd.exception.ApplicationAccessDeniedException;
import com.atlassian.crowd.exception.ExpiredCredentialException;
import com.atlassian.crowd.exception.InactiveAccountException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.InvalidAuthorizationTokenException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.service.soap.client.SecurityServerClient;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.ExpiredCredentialsException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.pam.UnsupportedTokenException;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A realm that authenticates and obtains its roles from a Atlassian Crowd server.
 * <p/>
 * The Crowd server as the concept of role and group memberships. Both of which can be can be mapped to Shiro roles.
 * This realm implementation allows the deployer to select either or both memberships to map to Shiro roles.
 * <h5>Crowd client configuration</h5>
 * <p/>
 * In your applicationContext.xml, add the following:
 * <pre>
 * {@code<!-- This will load Crowd SecurityServerClient stuff -->
 *  <import resource="classpath:org/obiba/security/crowd-context.xml" />
 *
 *  <bean id="crowdRealm" class="org.apache.shiro.realm.crowd.CrowdRealm" autowire="byType">
 *    <property name="roleSources">
 *      <bean class="java.util.EnumSet" factory-method="of">
 *        <constructor-arg>
 *          <bean class="org.apache.shiro.realm.crowd.RoleSource" factory-method="valueOf">
 *            <constructor-arg value="ROLES_FROM_CROWD_GROUPS" />
 *          </bean>
 *        </constructor-arg>
 *      </bean>
 *    </property>
 *    <property name="groupRolesMap">
 *      <map>
 *        <entry key="group1" value="SYSTEM_ADMINISTRATOR" />
 *        <entry key="group2" value="PARTICIPANT_MANAGER" />
 *      </map>
 *    </property>
 *  </bean>
 *  }
 * </pre>
 * You also need to tell where is the Crowd instance to SecurityServerClient by defining a crowd.properties file. You
 * can copy this file from your Crowd installation folder CROWD_INSTALL/client or from folder
 * obiba-commons/obiba-core/src/main/test/resources.
 * <p/>
 * Copy also the <b>crowd-ehcache.xml</b> file to configure caching.
 * <p/>
 * Add these 2 properties to your config file:
 * <pre>
 *   crowd.properties.path  = file:/config-path/crowd.properties
 *   crowd-ehcache.xml.path = file:/config-path/crowd-ehcache.xml
 * </pre>
 * Here is a template of crowd.properties:
 * <pre>
 *  application.name            = crowd_client
 *  application.password        = password
 *  application.login.url       = http://localhost:8095/crowd/console/
 *
 *  crowd.server.url            = http://localhost:8095/crowd/services/
 *  crowd.base.url              = http://localhost:8095/crowd/
 *
 *  session.isauthenticated     = session.isauthenticated
 *  session.tokenkey            = session.tokenkey
 *  session.validationinterval  = 2
 *  session.lastvalidation      = session.lastvalidation
 * </pre>
 *
 * @version $Rev: 1026849 $ $Date: 2010-10-24 11:08:56 -0700 (Sun, 24 Oct 2010) $
 * @see <a href="https://confluence.atlassian.com/display/CROWD/The+crowd.properties+File">https://confluence.atlassian.com/display/CROWD/The+crowd.properties+File</a>
 * @see <a href="https://confluence.atlassian.com/display/CROWD024/Passing+the+crowd.properties+File+as+an+Environment+Variable">https://confluence.atlassian.com/display/CROWD024/Passing+the+crowd.properties+File+as+an+Environment+Variable</a>
 * @see <a href="https://code.google.com/a/apache-extras.org/p/atlassian-crowd-realm">https://code.google.com/a/apache-extras.org/p/atlassian-crowd-realm</a>
 */
public class CrowdRealm extends AuthorizingRealm {

  private static final Logger LOG = LoggerFactory.getLogger(CrowdRealm.class);

  private SecurityServerClient securityServerClient;
  private EnumSet<RoleSource> roleSources = EnumSet.of(RoleSource.ROLES_FROM_CROWD_ROLES);
  private Map<String, String> groupRolesMap;

  /**
   * A simple constructor for a Shiro Crowd realm.
   * <p/>
   * It is expected that an initialized Crowd client will be subsequently
   * set using {@link #setSecurityServerClient(SecurityServerClient)}.
   */
  public CrowdRealm() {
  }

  /**
   * Initialize the Shiro Crowd realm with an instance of
   * {@link SecurityServerClient}.  The method {@link SecurityServerClient#authenticate}
   * is assumed to be called by the creator of this realm.
   *
   * @param securityServerClient an instance of {@link SecurityServerClient} to be used when communicating with the Crowd server
   */
  public CrowdRealm(SecurityServerClient securityServerClient) {
    if(securityServerClient == null) throw new IllegalArgumentException("Crowd client cannot be null");
    this.securityServerClient = securityServerClient;
  }

  /**
   * Set the client to use when communicating with the Crowd server.
   * <p/>
   * It is assumed that the Crowd client has already authenticated with the
   * Crowd server.
   *
   * @param securityServerClient the client to use when communicating with the Crowd server
   */
  public void setSecurityServerClient(SecurityServerClient securityServerClient) {
    this.securityServerClient = securityServerClient;
  }

  /**
   * Obtain the kinds of Crowd memberships that will serve as sources for
   * Shiro roles.
   *
   * @return an enum set of role source directives.
   */
  public EnumSet<RoleSource> getRoleSources() {
    return roleSources;
  }

  /**
   * Set the kinds of Crowd memberships that will serve as sources for
   * Shiro roles.
   *
   * @param roleSources an enum set of role source directives.
   */
  public void setRoleSources(EnumSet<RoleSource> roleSources) {
    this.roleSources = roleSources;
  }

  public void setGroupRolesMap(Map<String, String> groupRolesMap) {
    this.groupRolesMap = groupRolesMap;
  }

  /**
   * {@inheritDoc}
   */
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {

    LOG.trace("Collecting authorization info from realm {}", getName());

    SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();

    for(Object principal : principalCollection.fromRealm(getName())) {
      LOG.trace("Collecting roles from {}", principal);

      try {
        if(roleSources.contains(RoleSource.ROLES_FROM_CROWD_ROLES)) {
          LOG.trace("Collecting Shiro roles from Crowd role memberships");
          for(String role : securityServerClient.findRoleMemberships(principal.toString())) {
            addRole(authorizationInfo, role);
          }
        }

        if(roleSources.contains(RoleSource.ROLES_FROM_CROWD_GROUPS)) {
          LOG.trace("Collecting Shiro roles from Crowd group memberships");
          for(String group : securityServerClient.findGroupMemberships(principal.toString())) {
            addRole(authorizationInfo, group);
          }
        }
      } catch(RemoteException re) {
        throw new AuthorizationException("Unable to obtain Crowd group memberships for principal " + principal + ".",
            re);
      } catch(com.atlassian.crowd.exception.InvalidAuthenticationException e) {
        throw new AuthorizationException("Unable to obtain Crowd group memberships for principal " + principal + ".",
            e);
      } catch(com.atlassian.crowd.exception.InvalidAuthorizationTokenException e) {
        throw new AuthorizationException("Unable to obtain Crowd group memberships for principal " + principal + ".",
            e);
      } catch(UserNotFoundException e) {
        throw new AuthorizationException("Unable to obtain Crowd group memberships for principal " + principal + ".",
            e);
      }
    }

    return authorizationInfo;
  }

  private void addRole(SimpleAuthorizationInfo authorizationInfo, String role) {
    if(groupRolesMap == null) {
      LOG.trace("Adding role {}", role);
      authorizationInfo.addRole(role);
    } else {
      String mappedRole = groupRolesMap.get(role);
      if(mappedRole == null) {
        LOG.warn("Role {} is not mapped", role);
      } else {
        LOG.trace("Adding role {} (mapped from {})", mappedRole, role);
        authorizationInfo.addRole(mappedRole);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  protected AuthenticationInfo doGetAuthenticationInfo(
      AuthenticationToken authenticationToken) throws AuthenticationException {

    LOG.trace("Collecting authentication info from realm {}", getName());
    LOG.trace("securityServerClient: {}", securityServerClient);

    if(!(authenticationToken instanceof UsernamePasswordToken)) {
      throw new UnsupportedTokenException(
          "Unsupported token of type " + authenticationToken.getClass().getName() + ".  " + UsernamePasswordToken.class
              .getName() + " is required.");
    }

    UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;
    try {
      securityServerClient.authenticatePrincipalSimple(token.getUsername(), new String(token.getPassword()));
      return new SimpleAuthenticationInfo(token.getPrincipal(), token.getCredentials(), getName());
    } catch(RemoteException e) {
      throw new AuthenticationException("Unable to obtain authenticate principal " + token.getUsername() + " in Crowd.",
          e);
    } catch(InvalidAuthenticationException e) {
      throw new IncorrectCredentialsException("Incorrect credentials for principal " + token.getUsername() + " in " +
          "Crowd.", e);
    } catch(ApplicationAccessDeniedException e) {
      throw new AuthenticationException("Access denied for principal " + token.getUsername() + " in Crowd.", e);
    } catch(InvalidAuthorizationTokenException e) {
      throw new IncorrectCredentialsException("Incorrect credentials for principal " + token.getUsername() + " in " +
          "Crowd.", e);
    } catch(InactiveAccountException e) {
      throw new DisabledAccountException("Inactive principal " + token.getUsername() + " in Crowd.", e);
    } catch(ExpiredCredentialException e) {
      throw new ExpiredCredentialsException("Expired principal " + token.getUsername() + " in Crowd.", e);
    }
  }
}
