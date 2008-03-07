package org.obiba.core.runtime;

import org.junit.Assert;
import org.junit.Test;
import org.obiba.runtime.Version;

public class VersionTest {

  @Test
  public void testParseMajorMinor() {
    Version v = new Version("1.2");
    Assert.assertEquals(1, v.getMajor());
    Assert.assertEquals(2, v.getMinor());
    Assert.assertEquals(0, v.getMicro());
    Assert.assertEquals("", v.getQualifier());
  }

  @Test
  public void testParseMajorMinorAndQualifier() {
    Version v = new Version("1.0-SNAPSHOT");
    Assert.assertEquals(1, v.getMajor());
    Assert.assertEquals(0, v.getMinor());
    Assert.assertEquals(0, v.getMicro());
    Assert.assertEquals("SNAPSHOT", v.getQualifier());
  }

  @Test
  public void testParseMajorMinorMicroAndQualifier() {
    Version v = new Version("1.2.3_b4");
    Assert.assertEquals(1, v.getMajor());
    Assert.assertEquals(2, v.getMinor());
    Assert.assertEquals(3, v.getMicro());
    Assert.assertEquals("b4", v.getQualifier());
  }

}
