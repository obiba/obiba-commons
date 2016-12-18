/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SuppressWarnings("UnusedDeclaration")
public final class StreamUtil {

  private static final int BUFFER_SIZE = 4096;

  private StreamUtil() {

  }

  /**
   * Closes a {@code Closeable} by checking for non-null argument and catching {@code IOException}.
   *
   * @param closeable the instance to safely and silently close
   */
  public static void silentSafeClose(@Nullable Closeable closeable) {
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
   * @throws IOException when an exception occurs during an I/O operation (read or write)
   */
  public static long copy(InputStream in, OutputStream out) throws IOException {
    byte[] buffer = new byte[BUFFER_SIZE];

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
  public static byte[] read(InputStream in) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    copy(in, baos);
    return baos.toByteArray();
  }

  /**
   * Reads bytes from {@code in}, closes the {@code InputStream} and returns them as a byte array.
   *
   * @param in the stream to read bytes from
   * @return the array of bytes read from {@code in}
   * @throws IOException when an exception occurs during reading
   */
  public static byte[] readFully(InputStream in) throws IOException {
    try {
      return read(in);
    } finally {
      silentSafeClose(in);
    }
  }

  /**
   * @param in
   * @return
   * @throws IOException
   * @Deprecated use {@link #readLines(InputStream, String)}
   * @since 1.0.4
   */
  @Deprecated
  @edu.umd.cs.findbugs.annotations.SuppressWarnings("DM_DEFAULT_ENCODING")
  public static List<String> readLines(InputStream in) throws IOException {
    InputStreamReader reader = new InputStreamReader(in);
    return readLines(reader);
  }

  /**
   * @param in
   * @param encoding
   * @return
   * @throws IOException
   * @since 1.0.4
   */
  public static List<String> readLines(InputStream in, @Nonnull String encoding) throws IOException {
    return readLines(new InputStreamReader(in, encoding));
  }

  /**
   * @param input
   * @return
   * @throws IOException
   * @since 1.0.4
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
