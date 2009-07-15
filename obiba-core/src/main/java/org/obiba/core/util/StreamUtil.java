package org.obiba.core.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public final class StreamUtil {

  private StreamUtil() {

  }

  /**
   * Closes a {@code Closeable} by checking for non-null argument and catching {@code IOException}.
   * 
   * @param closeable the instance to safely and silently close
   */
  public static final void silentSafeClose(Closeable closeable) {
    try {
      if(closeable != null) {
        closeable.close();
      }
    } catch(IOException e) {
      // Silently ignore
    }
  }

  /**
   * Copies bytes read from {@code in} into {@code out}. Returns the number of bytes that were copied. Note that neither
   * {@code in} nor {@code out} are closed after copying.
   * 
   * @param in the stream to read from
   * @param out the stream to write to
   * @return the number of bytes copied
   * 
   * @throws IOException when an exception occurs during an I/O operation (read or write)
   */
  public static final long copy(final InputStream in, final OutputStream out) throws IOException {
    final byte[] buffer = new byte[4096];

    long bytesCopied = 0;
    int len;
    while((len = in.read(buffer)) > 0) {
      out.write(buffer, 0, len);
      bytesCopied += len;
    }
    return bytesCopied;
  }

  /**
   * Reads bytes from {@code in} and returns them as a byte array. Note that {@code in} is not closed after reading.
   * 
   * @param in the stream to read bytes from
   * @return the array of bytes read from {@code in}
   * @throws IOException when an exception occurs during reading
   */
  public static final byte[] read(final InputStream in) throws IOException {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    copy(in, baos);
    return baos.toByteArray();
  }

  /**
   * Reads bytes from {@code in}, closes the {@code InputStream} and returns them as a byte array.
   * @param in the stream to read bytes from
   * @return the array of bytes read from {@code in}
   * @throws IOException when an exception occurs during reading
   */
  public static final byte[] readFully(final InputStream in) throws IOException {
    try {
      return read(in);
    } finally {
      silentSafeClose(in);
    }
  }

  /**
   * 
   * @since 1.0.4
   * @param in
   * @return
   * @throws IOException
   */
  public static final List<String> readLines(final InputStream in) throws IOException {
    InputStreamReader reader = new InputStreamReader(in);
    return readLines(reader);
  }

  /**
   * @since 1.0.4
   * @param in
   * @param encoding
   * @return
   * @throws IOException
   */
  public static final List<String> readLines(final InputStream in, String encoding) throws IOException {
    if(encoding == null) {
      return readLines(in);
    } else {
      return readLines(new InputStreamReader(in, encoding));
    }
  }

  /**
   * 
   * @since 1.0.4
   * @param input
   * @return
   * @throws IOException
   */
  public static List<String> readLines(Reader in) throws IOException {
    BufferedReader reader = new BufferedReader(in);
    List<String> list = new ArrayList<String>();
    String line = reader.readLine();
    while(line != null) {
      list.add(line);
      line = reader.readLine();
    }
    return list;
  }

}
