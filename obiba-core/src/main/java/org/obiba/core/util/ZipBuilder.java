package org.obiba.core.util;

import de.idyl.winzipaes.AesZipFileEncrypter;
import de.idyl.winzipaes.impl.AESEncrypterBC;

import java.io.*;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

public class ZipBuilder {

  private final File output;

  private final ZipOutputStream outputStream;

  private int baseLength = 0;

  private String password = "";

  private AESEncrypterBC encrypter;

  private ZipBuilder(File output) throws FileNotFoundException {
    this.output = output;
    this.outputStream = new ZipOutputStream(new FileOutputStream(output));
  }

  /**
   * Prepare a zip file at the given location.
   *
   * @param output
   * @return
   * @throws FileNotFoundException
   */
  public static ZipBuilder newBuilder(File output) throws FileNotFoundException {
    return new ZipBuilder(output);
  }

  /**
   * Rebase the files relatively to the provided file.
   *
   * @param base
   * @return
   */
  public ZipBuilder base(File base) {
    String path = (base.isDirectory() ? base : base.getParentFile()).getAbsolutePath();
    if (!path.endsWith(File.separator)) {
      path = path + File.separator;
    }
    baseLength = path.length();
    return this;
  }

  /**
   * Encrypt files with a password.
   *
   * @param password
   * @return
   * @throws ZipException
   */
  public ZipBuilder password(String password) throws ZipException {
    this.password = password;
    this.encrypter = new AESEncrypterBC();
    this.encrypter.init(password, 0);
    return this;
  }

  /**
   * Apply best compression level.
   *
   * @return
   */
  public ZipBuilder compressed() {
    outputStream.setLevel(Deflater.BEST_COMPRESSION);
    return this;
  }

  /**
   * Zip file or folder (recursively) without file filter.
   *
   * @param entry
   * @return
   * @throws IOException
   */
  public ZipBuilder put(File entry) throws IOException {
    return put(entry, null);
  }

  /**
   * Zip file or folder (recursively) with file filter.
   *
   * @param entry
   * @param filter
   * @return
   * @throws IOException
   */
  public ZipBuilder put(File entry, FileFilter filter) throws IOException {
    if (entry.isDirectory()) {
      outputStream.putNextEntry(new ZipEntry(entry.getAbsolutePath().substring(baseLength) + "/"));
      File[] children = filter == null ? entry.listFiles() : entry.listFiles(filter);
      if (children != null) {
        for (File child : children) {
          put(child, filter);
        }
      }
    } else {
      outputStream.putNextEntry(new ZipEntry(entry.getAbsolutePath().substring(baseLength)));
      try(FileInputStream inputStream = new FileInputStream(entry)) {
        StreamUtil.copy(inputStream, outputStream);
        outputStream.closeEntry();
      }
    }
    return this;
  }

  /**
   * Get the zip file, encrypt it if a password was provided.
   *
   * @return
   * @throws IOException
   */
  public File build() throws IOException {
    outputStream.close();
    if (hasPassword()) {
      File tmp = File.createTempFile(output.getName(), ".zip");
      FileUtil.copyFile(output, tmp);
      output.delete();
      AesZipFileEncrypter.zipAndEncryptAll(tmp, output, password, encrypter);
      tmp.delete();
    }
    return output;
  }

  /**
   * Check if a password is provided.
   *
   * @return
   */
  private boolean hasPassword() {
    return password != null && password.length()>0;
  }
}
