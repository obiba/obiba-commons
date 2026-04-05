package org.obiba.shiro.tools.hasher;

import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.junit.Test;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class HasherTest {

  private final DefaultPasswordService passwordService = new DefaultPasswordService();

  @Test
  public void shouldGeneratePasswordServiceCompatibleHash() {
    String hash = Hasher.hash("password123");

    assertTrue(passwordService.passwordsMatch("password123", hash));
    assertTrue(hash.startsWith("$shiro2$argon2id$"));
  }

  @Test
  public void shouldGenerateDifferentHashesForSameValueBecauseOfRandomSalt() {
    String firstHash = Hasher.hash("password123");
    String secondHash = Hasher.hash("password123");

    assertNotEquals(firstHash, secondHash);
    assertTrue(passwordService.passwordsMatch("password123", firstHash));
    assertTrue(passwordService.passwordsMatch("password123", secondHash));
  }
}

