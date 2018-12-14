/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Simple check for duplicate jars and class files across the classpath.
 */
public class JarUtil {

  private static final Logger log = LoggerFactory.getLogger(JarUtil.class);

  private static final boolean DUPLICATE_JAR_STRICT = false;

  private JarUtil() {}

  /**
   * Checks the current classpath for duplicate classes
   *
   * @throws IllegalStateException if duplicate jar was found
   */
  public static void checkJars() throws IOException, URISyntaxException {
    ClassLoader loader = JarUtil.class.getClassLoader();
    if (log.isDebugEnabled()) {
      log.debug("java.class.path: {}", System.getProperty("java.class.path"));
      log.debug("sun.boot.class.path: {}", System.getProperty("sun.boot.class.path"));
      if (loader instanceof URLClassLoader) {
        log.debug("classloader urls: {}", Arrays.toString(((URLClassLoader) loader).getURLs()));
      }
    }
    checkJars(parseClassPath());
  }

  /**
   * Parses the classpath into an array of URLs
   *
   * @return array of URLs
   * @throws IllegalStateException if the classpath contains empty elements
   */
  public static Set<URL> parseClassPath() {
    return parseClassPath(System.getProperty("java.class.path"));
  }

  /**
   * Parses the classpath into a set of URLs. For testing.
   *
   * @param classPath classpath to parse (typically the system property {@code java.class.path})
   * @return array of URLs
   * @throws IllegalStateException if the classpath contains empty elements
   */
  static Set<URL> parseClassPath(String classPath) {
    String pathSeparator = System.getProperty("path.separator");
    String fileSeparator = System.getProperty("file.separator");
    String elements[] = classPath.split(pathSeparator);
    Set<URL> urlElements = new LinkedHashSet<>(); // order is already lost, but some filesystems have it
    for (String element : elements) {
      if (element.startsWith("/") && "\\".equals(fileSeparator)) {
        // "correct" the entry to become a normal entry
        // change to correct file separators
        element = element.replace("/", "\\");
        // if there is a drive letter, nuke the leading separator
        if (element.length() >= 3 && element.charAt(2) == ':') {
          element = element.substring(1);
        }
      }
      // now just parse as ordinary file
      try {
        URL url = FileUtil.getPath(element).toUri().toURL();
        if (!urlElements.add(url)) {
          if (log.isDebugEnabled())
            log.warn("Duplicate jar [{}] on classpath: {}", element, classPath);
          else
            log.warn("Duplicate jar [{}] on classpath", element);
          if (DUPLICATE_JAR_STRICT) throw new IllegalStateException("Duplicate jar [" + element + "] on classpath: " + classPath);
        }
      } catch (MalformedURLException e) {
        // should not happen, as we use the filesystem API
        throw new RuntimeException(e);
      }
    }
    return Collections.unmodifiableSet(urlElements);
  }

  /**
   * Checks the set of URLs for duplicate classes
   *
   * @throws IllegalStateException if duplicate jars were found
   */
  public static void checkJars(Set<URL> urls) throws URISyntaxException, IOException {
    // we don't try to be sneaky and use deprecated/internal/not portable stuff
    // like sun.boot.class.path, and with jigsaw we don't yet have a way to get
    // a "list" at all. So just exclude any elements underneath the java home
    String javaHome = System.getProperty("java.home");
    log.debug("java.home: {}", javaHome);
    final Map<String, Path> clazzes = new HashMap<>(32768);
    Set<Path> seenJars = new HashSet<>();
    for (final URL url : urls) {
      final Path path = FileUtil.getPath(url.toURI());
      // exclude system resources
      if (path.startsWith(javaHome)) {
        log.debug("excluding system resource: {}", path);
        continue;
      }
      if (path.toString().endsWith(".jar")) {
        if (!seenJars.add(path)) {
          log.warn("Duplicate jar on classpath: {}", path);
          if (DUPLICATE_JAR_STRICT) throw new IllegalStateException("Duplicate jar on classpath: " + path);
        }
        log.debug("examining jar: {}", path);
        try (JarFile file = new JarFile(path.toString())) {
          // inspect entries
          Enumeration<JarEntry> elements = file.entries();
          while (elements.hasMoreElements()) {
            String entry = elements.nextElement().getName();
            if (entry.endsWith(".class")) {
              // for jar format, the separator is defined as /
              entry = entry.replace('/', '.').substring(0, entry.length() - 6);
              checkClass(clazzes, entry, path);
            }
          }
        }
      } else {
        log.debug("examining directory: {}", path);
        // case for tests: where we have class files in the classpath
        final Path root = FileUtil.getPath(url.toURI());
        final String sep = root.getFileSystem().getSeparator();
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            String entry = root.relativize(file).toString();
            if (entry.endsWith(".class")) {
              // normalize with the os separator, remove '.class'
              entry = entry.replace(sep, ".").substring(0, entry.length() - ".class".length());
              checkClass(clazzes, entry, path);
            }
            return super.visitFile(file, attrs);
          }
        });
      }
    }
  }

  private static void checkClass(Map<String, Path> clazzes, String clazz, Path jarpath) {
    Path previous = clazzes.put(clazz, jarpath);
    if (previous != null) {
      if (previous.equals(jarpath)) {
        if (clazz.startsWith("org.apache.xmlbeans")) {
          return; // https://issues.apache.org/jira/browse/XMLBEANS-499
        }
        throw new IllegalStateException("Class: " + clazz + System.lineSeparator() + "exists multiple times in jar: " + jarpath);
      } else if (clazz.startsWith("org.joda.time") // packed in elasticsearch jar...
          || clazz.startsWith("com.sun.istack") // jaxb-core includes classes of and depends on istack-commons-runtime...
          || clazz.startsWith("org.xmlpull") // xmlpull includes classes of and depends on xpp3...
          || clazz.startsWith("javax.transaction") // jboss-transaction-api and ow2-jta
          || clazz.startsWith("javax.xml") //jsr173_api and stax-api
          || clazz.startsWith("org.apache.shiro") // shiro-lang and shiro-core badly packaged
          || clazz.startsWith("org.aopalliance") // spring-aop vs. aopalliance
          ) {
        return;
      } else {
        throw new IllegalStateException("Class: " + clazz + System.lineSeparator() +
            "jar1: " + previous + System.lineSeparator() +
            "jar2: " + jarpath);
      }
    }
  }

}

