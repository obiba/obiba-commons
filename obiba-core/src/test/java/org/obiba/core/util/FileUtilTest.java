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

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.zip.DataFormatException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import de.idyl.winzipaes.AesZipFileDecrypter;
import de.idyl.winzipaes.impl.AESDecrypterBC;
import de.idyl.winzipaes.impl.ExtZipEntry;
import org.junit.Assert;
import org.junit.Test;

public class FileUtilTest {

  @Test
  public void testCopyFile() throws IOException {

    File source = getSourceFile("logback-test.xml");
    File dest = File.createTempFile("test", "tmp");

    FileUtil.copyFile(source, dest);

    Assert.assertEquals(source.length(), dest.length());
    byte[] sourceBytes = StreamUtil.readFully(new FileInputStream(source));
    byte[] destBytes = StreamUtil.readFully(new FileInputStream(dest));
    Assert.assertTrue(Arrays.equals(sourceBytes, destBytes));
  }

  @Test
  @SuppressWarnings("ResultOfMethodCallIgnored")
  public void testCopyFileInDir() throws IOException {

    File source = getSourceFile("logback-test.xml");
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

  @Test
  public void testZipUnzip() throws IOException {
    testZipUnzip(null);
  }

  @Test
  public void testZipUnzipEncrypted() throws IOException {
    testZipUnzip("password");
  }

  private void testZipUnzip(String password) throws IOException {
    File zipped = File.createTempFile("testZipFolder", ".zip");
    FileUtil.zip(new File("src/test/resources/zip-test"), zipped, password);
    if (password == null) listEntries(zipped);
    else listEntries(zipped, password);
    File unzipped = Files.createTempDirectory("unzip-test").toFile();
    FileUtil.unzip(zipped, unzipped, password);
    listFiles(unzipped);
    File folder2 = new File(unzipped,"zip-test/folder2");
    Assert.assertTrue(folder2.exists());
    Assert.assertTrue(folder2.isDirectory());
    File file1 = new File(unzipped, "zip-test/folder1/file1.txt");
    Assert.assertTrue(file1.exists());
    Assert.assertFalse(file1.isDirectory());
    verifyContent(file1, "This is test file 1.");
    FileUtil.delete(unzipped);
    FileUtil.delete(zipped);
  }

  //
  // ZipBuilder tests
  //

  @Test
  public void testZipFolder() throws IOException {
    File output = File.createTempFile("testZipFolder", ".zip");
    output.deleteOnExit();
    ZipBuilder.newBuilder(output).put(new File("src/test/resources/zip-test")).build();

    listEntries(output);
    verifyEntries(output, 6);
  }

  @Test
  public void testZipFilteredFolder() throws IOException {
    File output = File.createTempFile("testZipFilteredFolder", ".zip");
    output.deleteOnExit();
    ZipBuilder.newBuilder(output).put(new File("src/test/resources/zip-test"), //
        pathname -> pathname.isDirectory() || pathname.getPath().endsWith(".xml")).build();

    listEntries(output);
    verifyEntries(output, 4);
  }

  @Test
  public void testZipBasedFolder() throws IOException {
    File output = File.createTempFile("testZipBasedFolder", ".zip");
    output.deleteOnExit();
    File file = new File("src/test/resources/zip-test");
    ZipBuilder.newBuilder(output).base(file.getParentFile()).put(file).build();

    listEntries(output);
    verifyEntries(output, 6, s -> s.startsWith("zip-test/"));
    verifyEntry(output, "zip-test/file0.txt", "This is test file 0, avec des caractères accentués.");
  }

  @Test
  public void testZipBasedFile() throws IOException {
    File output = File.createTempFile("testZipBasedFile", ".zip");
    output.deleteOnExit();
    File file = new File("src/test/resources/zip-test/file0.txt");
    ZipBuilder.newBuilder(output).base(file.getParentFile()).put(file).build();

    listEntries(output);
    verifyEntries(output, 1);
    verifyEntry(output, "file0.txt", "This is test file 0, avec des caractères accentués.");
  }

  @Test
  public void testZipCompressedFolder() throws IOException {
    File output = File.createTempFile("testZipCompressedFolder", ".zip");
    output.deleteOnExit();
    File file = new File("src/test/resources/zip-test");
    ZipBuilder.newBuilder(output).base(file.getParentFile()).compressed().put(file).build();

    listEntries(output);
    verifyEntries(output, 6);
    verifyEntry(output, "zip-test/file0.txt", "This is test file 0, avec des caractères accentués.");
  }

  @Test
  public void testEncryptedZipFolder() throws IOException, DataFormatException {
    File output = File.createTempFile("testEncryptedZipFolder", ".zip");
    output.deleteOnExit();
    File file = new File("src/test/resources/zip-test");
    ZipBuilder.newBuilder(output).base(file.getParentFile())
        .password("password").put(file).build();

    listEntries(output, "password");
    verifyEntries(output, 6, "password");
    verifyEntry(output, "zip-test/file0.txt", "password", "This is test file 0, avec des caractères accentués.");
  }

  private void listFiles(File file) throws IOException {
    System.out.println(file.getAbsolutePath());
    if (file.isDirectory()) {
      File[] children = file.listFiles();
      if (children != null) {
        for (File child : children)
          listFiles(child);
      }
    }
  }

  private void listEntries(File zip) throws IOException {
    ZipFile zipFile = new ZipFile(zip);
    zipFile.stream()
        .map(ZipEntry::getName)
        .forEach(System.out::println);
  }

  private void verifyEntries(File zip, int expectedCount) throws IOException {
    ZipFile zipFile = new ZipFile(zip);
    Assert.assertEquals(expectedCount, zipFile.stream().count());
  }

  private void verifyEntry(File zip, String name, String expected) throws IOException {
    ZipFile zipFile = new ZipFile(zip);
    ZipEntry entry = zipFile.getEntry(name);
    Assert.assertNotNull(entry);
    InputStream in = zipFile.getInputStream(entry);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    StreamUtil.copy(in, out);
    in.close();
    out.close();
    Assert.assertEquals(expected, out.toString("UTF-8"));
  }

  private void verifyEntries(File zip, int expectedCount, Predicate<String> predicate) throws IOException {
    ZipFile zipFile = new ZipFile(zip);
    Assert.assertEquals(expectedCount, zipFile.stream()
        .map(ZipEntry::getName).filter(predicate).count());
  }

  private void listEntries(File zip, String password) throws IOException {
    AesZipFileDecrypter ze = new AesZipFileDecrypter(zip, new AESDecrypterBC());
    ze.getEntryList().stream()
        .map(ZipEntry::getName)
        .forEach(System.out::println);
  }

  private void verifyEntries(File zip, int expectedCount, String password) throws IOException {
    AesZipFileDecrypter ze = new AesZipFileDecrypter(zip, new AESDecrypterBC());
    Assert.assertEquals(expectedCount, ze.getNumberOfEntries());
  }

  private void verifyEntry(File zip, String name, String password, String expected) throws IOException, DataFormatException {
    AesZipFileDecrypter ze = new AesZipFileDecrypter(zip, new AESDecrypterBC());
    ExtZipEntry entry = ze.getEntry(name);
    Assert.assertNotNull(entry);
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    ze.extractEntry(entry, outStream, password);
    System.out.println(outStream.toString("utf-8"));
    Assert.assertEquals(expected, outStream.toString());
  }

  private void verifyContent(File file, String expected) throws IOException {
    List<String> lines = Files.readAllLines(file.toPath());
    Assert.assertFalse(lines.isEmpty());
    Assert.assertEquals(expected, lines.get(0));
  }

  private File getSourceFile(String name) {
    File target = new File("target");
    File testRoot = new File(target, "test-classes");
    return new File(testRoot, name);
  }

}
