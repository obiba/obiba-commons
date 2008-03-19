package org.obiba.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtil {

  /**
   * Copy recursively one directory to another.
   * @param sourceDir
   * @param destDir
   * @throws IOException
   */
  public static void copyDirectory(File sourceDir, File destDir)
      throws IOException {

    if (!destDir.exists()) {
      destDir.mkdir();
    }

    File[] children = sourceDir.listFiles();

    for (File sourceChild : children) {
      String name = sourceChild.getName();
      File destChild = new File(destDir, name);
      if (sourceChild.isDirectory()) {
        copyDirectory(sourceChild, destChild);
      } else {
        copyFile(sourceChild, destChild);
      }
    }

  }

  /**
   * Copy a normal file to another.
   * @param source
   * @param dest destination directory or destination file
   * @throws IOException
   */
  public static void copyFile(File source, File dest) throws IOException {

    if (dest.isDirectory()) {
      dest = new File(dest, source.getName());
    }

    if (!dest.exists()) {
      dest.createNewFile();
    }

    InputStream in = null;
    OutputStream out = null;

    try {
      in = new FileInputStream(source);
      out = new FileOutputStream(dest);
      byte[] buf = new byte[1024];
      int len;

      while ((len = in.read(buf)) > 0) {
        out.write(buf, 0, len);
      }
    } finally {
      in.close();
      out.close();
    }
  }

  /**
   * Delete the normal file or delete recursively the directory.
   * @param resource
   * @return
   * @throws IOException
   */
  public static boolean delete(File resource) throws IOException {

    if (resource.isDirectory()) {
      File[] childFiles = resource.listFiles();
      for (File child : childFiles) {
        delete(child);
      }
    }

    return resource.delete();
  }

}
