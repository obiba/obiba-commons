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

import java.util.Arrays;
import java.util.EnumSet;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.junit.Assert;
import org.junit.Test;

import com.atlassian.crowd.service.soap.client.SecurityServerClient;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @version $Revision: $ $Date: $
 */
public class CrowdRealmTest {

  @Test
  public void testAuthentication() throws Exception {

    SecurityServerClient client = createStrictMock(SecurityServerClient.class);
    expect(client.authenticatePrincipalSimple("yoko", "barbie")).andReturn("UNUSED");
    replay(client);

    CrowdRealm realm = new CrowdRealm(client);
    realm.setName("NutHouse");

    AuthenticationInfo authenticationInfo = realm.doGetAuthenticationInfo(new UsernamePasswordToken("yoko", "barbie"));

    verify(client);
    assertNotNull(authenticationInfo);
    assertTrue(Arrays.equals("barbie".toCharArray(), (char[]) authenticationInfo.getCredentials()));

    PrincipalCollection collection = authenticationInfo.getPrincipals();
    assertNotNull(collection);
    assertTrue(!collection.isEmpty());
    Assert.assertEquals("yoko", collection.getPrimaryPrincipal());
    assertTrue(!collection.getRealmNames().isEmpty());
    assertTrue(collection.getRealmNames().contains("NutHouse"));
    assertTrue(!collection.fromRealm("NutHouse").isEmpty());
    assertTrue(collection.fromRealm("NutHouse").contains("yoko"));
  }

  @Test
  public void testDefaultRoles() throws Exception {

    SecurityServerClient client = createStrictMock(SecurityServerClient.class);
    expect(client.authenticatePrincipalSimple("yoko", "barbie")).andReturn("UNUSED");
    expect(client.findRoleMemberships("yoko")).andReturn(new String[] { "big_sister", "table_setter", "dog_walker" });
    replay(client);

    CrowdRealm realm = new CrowdRealm(client);
    realm.setName("NutHouse");

    AuthenticationInfo authenticationInfo = realm.doGetAuthenticationInfo(new UsernamePasswordToken("yoko", "barbie"));
    AuthorizationInfo authorizationInfo = realm.doGetAuthorizationInfo(authenticationInfo.getPrincipals());

    verify(client);
    assertTrue(!authorizationInfo.getRoles().isEmpty());
    assertTrue(authorizationInfo.getRoles().contains("big_sister"));
    assertTrue(authorizationInfo.getRoles().contains("table_setter"));
    assertTrue(authorizationInfo.getRoles().contains("dog_walker"));
  }

  @Test
  public void testRoleMemberships() throws Exception {

    SecurityServerClient client = createStrictMock(SecurityServerClient.class);
    expect(client.authenticatePrincipalSimple("yoko", "barbie")).andReturn("UNUSED");
    expect(client.findRoleMemberships("yoko")).andReturn(new String[] { "big_sister", "table_setter", "dog_walker" });
    replay(client);

    CrowdRealm realm = new CrowdRealm(client);
    realm.setName("NutHouse");
    realm.setRoleSources(EnumSet.of(RoleSource.ROLES_FROM_CROWD_ROLES));

    AuthenticationInfo authenticationInfo = realm.doGetAuthenticationInfo(new UsernamePasswordToken("yoko", "barbie"));
    AuthorizationInfo authorizationInfo = realm.doGetAuthorizationInfo(authenticationInfo.getPrincipals());

    verify(client);
    assertTrue(!authorizationInfo.getRoles().isEmpty());
    assertTrue(authorizationInfo.getRoles().contains("big_sister"));
    assertTrue(authorizationInfo.getRoles().contains("table_setter"));
    assertTrue(authorizationInfo.getRoles().contains("dog_walker"));
  }

  @Test
  public void testGroupMemberships() throws Exception {

    SecurityServerClient client = createStrictMock(SecurityServerClient.class);
    expect(client.authenticatePrincipalSimple("yoko", "barbie")).andReturn("UNUSED");
    expect(client.findGroupMemberships("yoko")).andReturn(new String[] { "girls", "naughty" });
    replay(client);

    CrowdRealm realm = new CrowdRealm(client);
    realm.setName("NutHouse");
    realm.setRoleSources(EnumSet.of(RoleSource.ROLES_FROM_CROWD_GROUPS));

    AuthenticationInfo authenticationInfo = realm.doGetAuthenticationInfo(new UsernamePasswordToken("yoko", "barbie"));
    AuthorizationInfo authorizationInfo = realm.doGetAuthorizationInfo(authenticationInfo.getPrincipals());

    verify(client);
    assertTrue(!authorizationInfo.getRoles().isEmpty());
    assertTrue(authorizationInfo.getRoles().contains("girls"));
    assertTrue(authorizationInfo.getRoles().contains("naughty"));
  }

  @Test
  public void testAll() throws Exception {

    SecurityServerClient client = createStrictMock(SecurityServerClient.class);
    expect(client.authenticatePrincipalSimple("yoko", "barbie")).andReturn("UNUSED");
    expect(client.findRoleMemberships("yoko")).andReturn(new String[] { "big_sister", "table_setter", "dog_walker" });
    expect(client.findGroupMemberships("yoko")).andReturn(new String[] { "girls", "naughty" });
    replay(client);

    CrowdRealm realm = new CrowdRealm(client);
    realm.setName("NutHouse");
    realm.setRoleSources(EnumSet.of(RoleSource.ROLES_FROM_CROWD_GROUPS, RoleSource.ROLES_FROM_CROWD_ROLES));

    AuthenticationInfo authenticationInfo = realm.doGetAuthenticationInfo(new UsernamePasswordToken("yoko", "barbie"));
    AuthorizationInfo authorizationInfo = realm.doGetAuthorizationInfo(authenticationInfo.getPrincipals());

    verify(client);
    assertTrue(!authorizationInfo.getRoles().isEmpty());
    assertTrue(authorizationInfo.getRoles().contains("big_sister"));
    assertTrue(authorizationInfo.getRoles().contains("table_setter"));
    assertTrue(authorizationInfo.getRoles().contains("dog_walker"));
    assertTrue(authorizationInfo.getRoles().contains("girls"));
    assertTrue(authorizationInfo.getRoles().contains("naughty"));
  }

  public void testIntegration() throws Exception {

    CrowdRealm realm = new CrowdRealm();
    realm.setName("NutHouse");
    realm.setRoleSources(EnumSet.of(RoleSource.ROLES_FROM_CROWD_GROUPS, RoleSource.ROLES_FROM_CROWD_ROLES));

    AuthenticationInfo authenticationInfo = realm.doGetAuthenticationInfo(new UsernamePasswordToken("yoko", "barbie"));

    assertNotNull(authenticationInfo);
    assertTrue(Arrays.equals("barbie".toCharArray(), (char[]) authenticationInfo.getCredentials()));

    PrincipalCollection collection = authenticationInfo.getPrincipals();
    assertNotNull(collection);
    assertTrue(!collection.isEmpty());
    Assert.assertEquals("yoko", collection.getPrimaryPrincipal());
    assertTrue(!collection.getRealmNames().isEmpty());
    assertTrue(collection.getRealmNames().contains("NutHouse"));
    assertTrue(!collection.fromRealm("NutHouse").isEmpty());
    assertTrue(collection.fromRealm("NutHouse").contains("yoko"));

    AuthorizationInfo authorizationInfo = realm.doGetAuthorizationInfo(authenticationInfo.getPrincipals());

    assertTrue(!authorizationInfo.getRoles().isEmpty());
    assertTrue(authorizationInfo.getRoles().contains("big_sister"));
    assertTrue(authorizationInfo.getRoles().contains("table_setter"));
    assertTrue(authorizationInfo.getRoles().contains("dog_walker"));
    assertTrue(authorizationInfo.getRoles().contains("girls"));
    assertTrue(authorizationInfo.getRoles().contains("naughty"));
  }
}
