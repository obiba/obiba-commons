package org.obiba.wicket.util.resource;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Locale;

import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Build a csv formatted resource stream. Data are buffered in a temporary file, using
 * ISO-8859-1 char set.
 *
 * @author ymarcon
 */
public class CsvResourceStream implements IResourceStream {

  private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(CsvResourceStream.class);

  public static final String FILE_SUFFIX = "xls";

  /**
   * This is for having a charset, understandable by excel...
   */
  private final Charset charset = Charset.forName("ISO-8859-1");

  private FileResourceStream fileResource = null;

  private File tmpFile;

  private BufferedOutputStream buffer;

  private Boolean isLineFirstValue = true;

  public CsvResourceStream() {
    try {
      tmpFile = File.createTempFile("CsvResourceStream_", ".csv");
      FileOutputStream outFile = new FileOutputStream(tmpFile);
      buffer = new BufferedOutputStream(outFile);
    } catch(IOException e) {
      log.error("IOException", e);
    }
  }

  /**
   * Append a data field.
   *
   * @param o
   */
  public void append(Object o) {
    //Separate value if it is not the first one one the current line.
    String appendedValue = "";
    if(isLineFirstValue) {
      isLineFirstValue = false;
    } else {
      appendedValue = getValueSeparator();
    }
    appendedValue += formatCsvString(o);
    internalAppend(appendedValue);
  }

  /**
   * Append a new line.
   */
  public void appendLine() {
    internalAppend(getLineSeparator());
    isLineFirstValue = true;
  }

  /**
   * End the data stream buffering and prepare for resource streaming.
   */
  public void appendEnd() {

    try {
      buffer.flush();
      buffer.close();
    } catch(IOException ignored) {
    }
    fileResource = new FileResourceStream(tmpFile);

  }

  /**
   * Write data in the temporary file.
   *
   * @param txt
   */
  private void internalAppend(String txt) {
    try {
      buffer.write(txt.getBytes(charset.name()));
    } catch(UnsupportedEncodingException e) {
      log.error("UnsupportedEncodingException", e);
    } catch(IOException e) {
      log.error("IOException", e);
    }
  }

  /**
   * Format a csv compliant string.
   *
   * @param o
   * @return
   */
  private String formatCsvString(Object o) {
    String txt = "";
    if(o != null) {
      txt = "\"" + o + "\"";
    }

    return txt;
  }

  @Override
  public void close() throws IOException {
    fileResource.close();
    tmpFile.delete();
  }

  @Override
  public String getContentType() {
    return "text/csv; charset=" + charset.name();
  }

  @Override
  public InputStream getInputStream() throws ResourceStreamNotFoundException {
    return fileResource.getInputStream();
  }

  @Override
  public Locale getLocale() {
    return fileResource.getLocale();
  }

  @Override
  public long length() {
    return fileResource.length();
  }

  @Override
  public void setLocale(Locale locale) {
    fileResource.setLocale(locale);
  }

  @Override
  public Time lastModifiedTime() {
    return fileResource.lastModifiedTime();
  }

  public String toString() {
    return fileResource == null
        ? "no file resource"
        : getContentType() + " " + fileResource.getFile().getAbsolutePath();
  }

  protected String getValueSeparator() {
    return "\t";
  }

  protected String getLineSeparator() {
    return "\r\n";
  }
}
