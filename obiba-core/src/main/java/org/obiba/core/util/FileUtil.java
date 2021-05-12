/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.core.util;

import de.idyl.winzipaes.AesZipFileDecrypter;
import de.idyl.winzipaes.impl.AESDecrypterBC;
import de.idyl.winzipaes.impl.ExtZipEntry;

import javax.annotation.Nonnull;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.DataFormatException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

@SuppressWarnings("UnusedDeclaration")
public final class FileUtil {

  private static final FileSystem DEFAULT_FS = FileSystems.getDefault();

  private FileUtil() {
  }

  @Nonnull
  public static File getFileFromResource(String path) {
    try {
      URL resource = FileUtil.class.getClassLoader().getResource(path);
      URI uri = resource == null ? null : resource.toURI();
      if (uri == null) throw new IllegalArgumentException("Cannot find file at " + path);
      return new File(uri);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("Cannot find file at " + path);
    }
  }

  /**
   * Copy recursively one directory to another.
   *
   * @param sourceDir
   * @param destDir
   * @throws IOException
   */
  public static void copyDirectory(File sourceDir, File destDir) throws IOException {

    if (!destDir.exists()) {
      if (!destDir.mkdir()) {
        throw new IOException("cannot create destination directory " + destDir.getAbsolutePath());
      }
    }

    File[] children = sourceDir.listFiles();
    if (children == null) return;

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
   * Copy a normal file to another. When {@code dest} exists and is a normal file, it is overwritten. When {@code dest}
   * is a directory, the {@code source} file is copied into that directory, that is, a new file is created within
   * {@code dest} using the same name as {@code source}.
   *
   * @param source the file to copy
   * @param dest   destination file or directory
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
   * @deprecated Use com.google.common.io.Files#move(java.io.File, java.io.File)
   */
  @Deprecated
  public static void moveFile(File source, File dest) throws IOException {
    File destFile = dest.isDirectory() ? new File(dest, source.getName()) : dest;
    if (!source.renameTo(destFile)) {
      copyFile(source, destFile);
      if (!source.delete()) {
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

    if (resource.isDirectory()) {
      File[] childFiles = resource.listFiles();
      if (childFiles != null) {
        for (File child : childFiles) {
          delete(child);
        }
      }
    }

    return resource.delete();
  }

  /**
   * Zip and compress a file/folder into a destination file.
   *
   * @param source
   * @param destination
   * @return
   * @throws IOException
   */
  public static File zip(File source, File destination) throws IOException {
    return zip(source, null, destination, null);
  }

  /**
   * Zip and compress a file/folder into an encrypted destination file.
   *
   * @param source
   * @param destination
   * @param password
   * @return
   * @throws IOException
   */
  public static File zip(File source, File destination, String password) throws IOException {
    return zip(source, null, destination, password);
  }

  /**
   * Zip and compress a folder into an encrypted destination file.
   *
   * @param source
   * @param sourceFilter
   * @param destination
   * @param password
   * @return
   * @throws IOException
   */
  public static File zip(File source, FileFilter sourceFilter, File destination, String password) throws IOException {
    return ZipBuilder.newBuilder(destination).base(source.getParentFile()).password(password).compressed().put(source, sourceFilter).build();
  }

  /**
   * Unzip an archive file in a folder.
   *
   * @param source
   * @param destination
   * @return
   * @throws IOException
   */
  public static File unzip(File source, File destination) throws IOException {
    ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(source.getAbsolutePath()));
    byte[] buffer = new byte[1024];

    ZipEntry zipEntry = zipInputStream.getNextEntry();
    while(zipEntry != null) {
      File file = zipEntryAsFile(destination, zipEntry);

      if (zipEntry.isDirectory()) {
        if (!file.isDirectory() && !file.mkdirs()) {
          throw new IOException("Can't create directory: " + file.getName());
        }
      } else {
        File parentFile = file.getParentFile();

        if (!parentFile.isDirectory() && !parentFile.mkdirs()) {
          throw new IOException("Can't create directory: " + parentFile.getName());
        }

        FileOutputStream fileOutputStream = new FileOutputStream(file);
        int length;
        while ((length = zipInputStream.read(buffer)) > 0) {
          fileOutputStream.write(buffer, 0, length);
        }
        fileOutputStream.close();
      }

      zipEntry = zipInputStream.getNextEntry();
    }

    return destination;
  }

  /**
   * Unzip a possibly encrypted archive file in a folder.
   *
   * @param source
   * @param destination
   * @param password    Archive content is encrypted if not null
   * @return
   * @throws IOException
   */
  public static File unzip(File source, File destination, String password) throws IOException {
    if (password == null) return unzip(source, destination);
    AesZipFileDecrypter ze = null;
    try {
      ze = new AesZipFileDecrypter(source, new AESDecrypterBC());
      for (ExtZipEntry entry : ze.getEntryList()) {
        File file = zipEntryAsFile(destination, entry);

        if (entry.isDirectory()) {
          if (!file.isDirectory() && !file.mkdirs()) {
            throw new IOException("Can't create directory: " + file.getName());
          }
        } else {
          File parentFile = file.getParentFile();

          if (!parentFile.isDirectory() && !parentFile.mkdirs()) {
            throw new IOException("Can't create directory: " + parentFile.getName());
          }

          ze.extractEntry(entry, file, password);
        }
      }
    } catch (DataFormatException | IOException e) {
      //
    } finally {
      if (ze != null) {
        try {
          ze.close();
        } catch (IOException e) {
          // ignore
        }
      }
    }
    return destination;
  }

  /**
   * Get path from URI.
   *
   * @param uri
   * @return
   */
  public static Path getPath(URI uri) {
    if (uri.getScheme().equalsIgnoreCase("file")) {
      return DEFAULT_FS.provider().getPath(uri);
    } else {
      return Paths.get(uri);
    }
  }

  /**
   * Converts a path string, or a sequence of strings that when joined form
   * a path string, to a {@code Path}.
   *
   * @param first
   * @param more
   * @return
   */
  public static Path getPath(String first, String... more) {
    return DEFAULT_FS.getPath(first, more);
  }

  private static File zipEntryAsFile(File destinationDirectory, ZipEntry zipEntry) throws IOException {
    File destinationFile = new File(destinationDirectory, zipEntry.getName());

    String destinationDirectoryCanonicalPath = destinationDirectory.getCanonicalPath();
    String destinationFileCanonicalPath = destinationFile.getCanonicalPath();

    if (!destinationFileCanonicalPath.startsWith(destinationDirectoryCanonicalPath + File.separator)) {
      throw new IOException("Zip entry and destination mismatch for: " + zipEntry.getName());
    }

    return destinationFile;
  }
}
