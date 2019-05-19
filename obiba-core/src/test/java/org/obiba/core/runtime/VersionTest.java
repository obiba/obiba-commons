/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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

  @Test
  public void testCompare() {
    Version v1 = new Version("1.2");
    Version v2 = new Version("1.2.1");
    Assert.assertTrue(v1.compareTo(v2) < 0);
    Assert.assertTrue(v2.compareTo(v1) > 0);
  }

  @Test
  public void testCompareWithQualifier() {
    Version v1 = new Version("1.2.1-b1");
    Version v2 = new Version("1.2.1-b2");
    Assert.assertTrue(v1.compareTo(v2) < 0);
    Assert.assertTrue(v2.compareTo(v1) > 0);
  }

  @Test
  public void testCompareWithQualifierAndNoQualifier() {
    Version v1 = new Version("1.2.1");
    Version v2 = new Version("1.2.1-b1");
    Assert.assertTrue(v1.compareTo(v2) < 0);
    Assert.assertTrue(v2.compareTo(v1) > 0);
  }

  @Test
  public void testEquals() {
    Version v1 = new Version("1.2");
    Version v2 = new Version(1, 2);

    Assert.assertTrue(v1.equals(v2));
    Assert.assertEquals(v1.hashCode(), v2.hashCode());
  }

  @Test
  public void testEqualsWithQualifier() {
    Version v1 = new Version("1.2-b4");
    Version v2 = new Version(1, 2, 0, "b4");

    Assert.assertTrue(v1.equals(v2));
    Assert.assertEquals(v1.hashCode(), v2.hashCode());
  }

}
