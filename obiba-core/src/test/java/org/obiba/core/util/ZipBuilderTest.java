package org.obiba.core.util;

import de.idyl.winzipaes.AesZipFileDecrypter;
import de.idyl.winzipaes.impl.AESDecrypterBC;
import de.idyl.winzipaes.impl.ExtZipEntry;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Predicate;
import java.util.zip.DataFormatException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipBuilderTest {

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
    //output.deleteOnExit();
    ZipBuilder.newBuilder(output).base(new File(".")).put(new File("src/test/resources/zip-test")).build();

    listEntries(output);
    verifyEntries(output, 6, s -> s.startsWith("src/"));
    //verifyEntry(output, "src/test/resources/zip-test/file0.txt", "This is test file 0, avec des caractères accentués.");
  }

  @Test
  public void testZipCompressedFolder() throws IOException {
    File output = File.createTempFile("testZipCompressedFolder", ".zip");
    output.deleteOnExit();
    ZipBuilder.newBuilder(output).compressed().put(new File("src/test/resources/zip-test")).build();

    listEntries(output);
    verifyEntries(output, 6);
    //verifyEntry(output, "src/test/resources/zip-test/file0.txt", "This is test file 0, avec des caractères accentués.");
  }

  @Test
  public void testEncryptedZipFolder() throws IOException, DataFormatException {
    File output = File.createTempFile("testEncryptedZipFolder", ".zip");
    //output.deleteOnExit();
    ZipBuilder.newBuilder(output).base(new File("."))
        .password("password").put(new File("src/test/resources/zip-test")).build();

    listEntries(output, "password");
    verifyEntries(output, 6, "password");
    //verifyEntry(output, "src/test/resources/zip-test/file0.txt", "password", "This is test file 0, avec des caractères accentués.");
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
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    ze.extractEntry(entry, outStream, password);
    System.out.println(outStream.toString("utf-8"));
    Assert.assertEquals(expected, outStream.toString());
  }
}
