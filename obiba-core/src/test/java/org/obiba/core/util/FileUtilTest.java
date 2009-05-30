package org.obiba.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;

public class FileUtilTest {

  @Test
  public void testCopyFile() throws IOException {

    File source = getSourceFile("log4j.properties");
    File dest = File.createTempFile("test", "tmp");

    FileUtil.copyFile(source, dest);

    Assert.assertEquals(source.length(), dest.length());
    byte[] sourceBytes = StreamUtil.readFully(new FileInputStream(source));
    byte[] destBytes = StreamUtil.readFully(new FileInputStream(dest));
    Assert.assertTrue(Arrays.equals(sourceBytes, destBytes));
  }

  @Test
  public void testCopyFileInDir() throws IOException {

    File source = getSourceFile("log4j.properties");
    File dest = File.createTempFile("test", "tmp");
    dest.delete();
    dest.mkdir();

    FileUtil.copyFile(source, dest);

    File newDest = new File(dest, source.getName());

    Assert.assertTrue(newDest.exists());
    Assert.assertEquals(source.length(), newDest.length());
    byte[] sourceBytes = StreamUtil.readFully(new FileInputStream(source));
    byte[] destBytes = StreamUtil.readFully(new FileInputStream(newDest));
    Assert.assertTrue(Arrays.equals(sourceBytes, destBytes));
  }

  private File getSourceFile(String name) {

    File target = new File("target");
    File testRoot = new File(target, "test-classes");

    return new File(testRoot, name);

  }
}
