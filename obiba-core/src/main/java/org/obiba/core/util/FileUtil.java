package org.obiba.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@SuppressWarnings("UnusedDeclaration")
public final class FileUtil {

  private FileUtil() {}

  /**
   * Copy recursively one directory to another.
   *
   * @param sourceDir
   * @param destDir
   * @throws IOException
   */
  public static void copyDirectory(File sourceDir, File destDir) throws IOException {

    if(!destDir.exists()) {
      if(!destDir.mkdir()) {
        throw new IOException("cannot create destination directory " + destDir.getAbsolutePath());
      }
    }

    File[] children = sourceDir.listFiles();
    if(children == null) return;

    for(File sourceChild : children) {
      String name = sourceChild.getName();
      File destChild = new File(destDir, name);
      if(sourceChild.isDirectory()) {
        copyDirectory(sourceChild, destChild);
      } else {
        copyFile(sourceChild, destChild);
      }
    }

  }

  /**
   * Copy a normal file to another. When {@code dest} exists and is a normal file, it is overwritten. When {@code dest}
   * is a directory, the {@code source} file is copied into that directory, that is, a new file is created within
   * {@code dest} using the same name as {@code source}.
   *
   * @param source the file to copy
   * @param dest destination file or directory
   * @return true if the destination file did not already exist and was created, false otherwise.
   * @throws IOException
   */
  public static boolean copyFile(File source, File dest) throws IOException {

    File destFile = dest.isDirectory() ? new File(dest, source.getName()) : dest;

    // Returns true when file did not already exist and was created, false if it already existed.
    boolean created = destFile.createNewFile();

    InputStream in = null;
    OutputStream out = null;
    try {
      StreamUtil.copy(in = new FileInputStream(source), out = new FileOutputStream(destFile));
      return created;
    } finally {
      StreamUtil.silentSafeClose(in);
      StreamUtil.silentSafeClose(out);
    }
  }

  /**
   * Move file to a directory or a new file.
   *
   * @param source
   * @param dest
   * @throws IOException
   */
  public static void moveFile(File source, File dest) throws IOException {
    File destFile = dest.isDirectory() ? new File(dest, source.getName()) : dest;
    if(!source.renameTo(destFile)) {
      copyFile(source, destFile);
      if(!source.delete()) {
        throw new IOException("Cannot delete source file " + source.getAbsolutePath());
      }
    }
  }

  /**
   * Delete the normal file or delete recursively the directory.
   *
   * @param resource
   * @return
   * @throws IOException
   */
  public static boolean delete(File resource) throws IOException {

    if(resource.isDirectory()) {
      File[] childFiles = resource.listFiles();
      if(childFiles != null) {
        for(File child : childFiles) {
          delete(child);
        }
      }
    }

    return resource.delete();
  }

}
