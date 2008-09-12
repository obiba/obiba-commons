package org.obiba.onyx.core.service;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.obiba.core.service.PagingClause;
import org.obiba.core.service.PersistenceManager;
import org.obiba.core.service.SortingClause;
import org.obiba.core.test.spring.BaseDefaultSpringContextTestCase;
import org.obiba.core.test.spring.Dataset;
import org.obiba.onyx.core.domain.user.Role;
import org.obiba.onyx.core.domain.user.Status;
import org.obiba.onyx.core.domain.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class UserServiceTest extends BaseDefaultSpringContextTestCase {
  
  @Autowired(required=true)
  PersistenceManager persistenceManager;
  
  @Autowired(required = true)
  UserService userService;
  
  @Test
  @Dataset
  public void testGetRoles() {
    List<Role> roleList = userService.getRoles(new SortingClause("name"));
    Assert.assertEquals(3, roleList.size());
  }
  
  @Test
  @Dataset
  public void testGetUsers() {
    List<User> userList = userService.getUsers(false, new PagingClause(0), new SortingClause("lastName"));
    Assert.assertEquals(3, userList.size());
  }
  
  @Test
  @Dataset
  public void testGetUserCount() {
    Assert.assertEquals(3, userService.getUserCount());
  }
  
  @Test
  @Dataset
  public void testGetUserWithLogin() {
    User user = userService.getUserWithLogin("ndupont");
    Assert.assertEquals("Dupont", user.getLastName());
    Assert.assertEquals("Natasha", user.getFirstName());
  }
  
  @Test
  @Dataset
  public void testChangeStatus() {
    userService.changeStatus(persistenceManager.get(User.class, Long.valueOf("2")), Status.INACTIVE);
    Assert.assertEquals(Status.INACTIVE, persistenceManager.get(User.class, Long.valueOf("2")).getStatus());
  }
  
  @Test
  @Dataset
  public void testDeleteUser() {
    userService.deleteUser(persistenceManager.get(User.class, Long.valueOf("2")));
    Assert.assertEquals(true, persistenceManager.get(User.class, Long.valueOf("2")).isDeleted());
  }
  
  @Test
  @Dataset
  public void testSetUserlanguage() {
    userService.setUserLanguage(persistenceManager.get(User.class, Long.valueOf("2")), Locale.FRENCH);
    Assert.assertEquals(Locale.FRENCH, persistenceManager.get(User.class, Long.valueOf("2")).getLanguage());
  }
  
  @Test
  @Dataset
  public void testIsNewPassword() {
    Assert.assertEquals(true, userService.isNewPassword(persistenceManager.get(User.class, Long.valueOf("2")), "turlututu"));
  }
 
  @Test
  @Dataset
  public void testSetpassword() {
    userService.setPassword(persistenceManager.get(User.class, Long.valueOf("2")), "newpasswordforuser");
    Assert.assertEquals("newpasswordforuser", persistenceManager.get(User.class, Long.valueOf("2")).getPassword());
  }
  
  @Test
  @Dataset
  public void testSetUser() {
    User modifUser = persistenceManager.get(User.class, Long.valueOf("3"));
    modifUser.setFirstName("Paul");
    userService.setUser(modifUser);
    Assert.assertEquals("Paul", persistenceManager.get(User.class, Long.valueOf("3")).getFirstName());
    
    User newUser = new User();
    newUser.setLastName("Tremblay");
    newUser.setFirstName("Michel");
    newUser.setLogin("mtremblay");
    newUser.setEmail("tremblay@obiba.org");
    newUser.setPassword("titi");
    newUser.setLanguage(Locale.ENGLISH);
    newUser.setStatus(Status.ACTIVE);
    Set<Role> roles = new HashSet<Role>();
    roles.add(Role.PARTICIPANT_MANAGER);
    newUser.setRoles(roles);
    
    userService.setUser(newUser);
    Assert.assertEquals("Tremblay", userService.getUserWithLogin("mtremblay").getLastName());
    Assert.assertEquals(Long.valueOf("4"), userService.getUserWithLogin("mtremblay").getId());    
  }
  
}