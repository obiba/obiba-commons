/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.plugins;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import org.obiba.core.util.JarUtil;
import org.obiba.runtime.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public abstract class PluginResources {

  private static final Logger log = LoggerFactory.getLogger(PluginResources.class);

  public static final String PLUGIN_DIST_SUFFIX = "-dist.zip";

  public static final String UNINSTALL_FILE = "uninstall";

  public static final String PLUGIN_PROPERTIES = "plugin.properties";

  public static final String SITE_PROPERTIES = "site.properties";

  private final File directory;

  private final File properties;

  private final File siteProperties;

  private final File lib;

  private final File uninstallFile;

  public PluginResources(File directory) {
    this.directory = directory;
    this.properties = new File(directory, PLUGIN_PROPERTIES);
    this.siteProperties = new File(directory, SITE_PROPERTIES);
    this.uninstallFile = new File(directory, UNINSTALL_FILE);
    this.lib = new File(directory, "lib");
  }

  public String getName() {
    try (FileInputStream in = new FileInputStream(properties)) {
      Properties prop = new Properties();
      prop.load(in);
      return prop.getProperty("name", directory.getName());
    } catch (Exception e) {
      log.warn("Failed reading plugin name property: {}", properties.getAbsolutePath(), e);
      return directory.getName();
    }
  }

  public String getType() {
    return getProperties().getProperty("type", "");
  }

  public boolean isValid() {
    return directory.isDirectory() && directory.canRead()
        && properties.exists() && properties.canRead()
        && lib.exists() && lib.isDirectory() && lib.canRead()
        && !uninstallFile.exists();
  }

  public Version getVersion() {
    String version = getProperties().getProperty("version", "0.0.0");
    return new Version(version);
  }

  public String getTitle() {
    return getProperties().getProperty("title", "");
  }

  public String getDescription() {
    return getProperties().getProperty("description", "");
  }

  public String getAuthor() {
    return getProperties().getProperty("author", "-");
  }

  public String getMaintainer() {
    return getProperties().getProperty("maintainer", "-");
  }

  public String getLicense() {
    return getProperties().getProperty("license", "-");
  }

  public String getWebsite() {
    return getProperties().getProperty("website");
  }

  public abstract String getHostVersionKey();

  public abstract String getHostHome();

  public Version getHostVersion() {
    String version = getProperties().getProperty(getHostVersionKey(), "0.0.0");
    return new Version(version);
  }

  public Properties getProperties() {
    Properties prop = getDefaultProperties();
    try (FileInputStream in = new FileInputStream(properties)) {
      prop.load(in);
    } catch (Exception e) {
      log.warn("Failed reading properties: {}", properties.getAbsolutePath(), e);
    }
    if (siteProperties.exists()) {
      try (FileInputStream in = new FileInputStream(siteProperties)) {
        prop.load(in);
      } catch (Exception e) {
        log.warn("Failed reading site properties: {}", siteProperties.getAbsolutePath(), e);
      }
    }
    return prop;
  }

  public boolean isToUninstall() {
    return uninstallFile.exists();
  }

  public File getDirectory() {
    return directory;
  }

  private Properties getDefaultProperties() {
    String name = getName();
    String home = getHostHome();
    Properties defaultProperties = new Properties();
    defaultProperties.put("OPAL_HOME", home);
    File dataDir = new File(home, "data" + File.separator + name);
    dataDir.mkdirs();
    defaultProperties.put("data.dir", dataDir.getAbsolutePath());
    File workDir = new File(home, "work" + File.separator + name);
    workDir.mkdirs();
    defaultProperties.put("work.dir", workDir.getAbsolutePath());
    defaultProperties.put("install.dir", directory.getAbsolutePath());
    return defaultProperties;
  }

  public URLClassLoader getURLClassLoader() {
    return getURLClassLoader(true);
  }

  public URLClassLoader getURLClassLoader(boolean checkJars) {
    File[] libs = lib.listFiles();
    URL[] urls = new URL[libs.length];
    for (int i = 0; i < libs.length; i++) {
      try {
        File lib = libs[i];
        urls[i] = lib.toURI().toURL();
        log.info("Adding library file to class loader: {}", lib);
      } catch (Exception e) {
        log.warn("Failed adding library file to class loader: {}", lib, e);
      }
    }
    if (checkJars) checkJars(urls);
    return new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
  }

  public void init() {
    init(true);
  }

  public void init(boolean checkJars) {
    File[] libs = lib.listFiles();
    URL[] urls = new URL[libs.length];
    if (libs == null) return;
    for (int i = 0; i < libs.length; i++) {
      try {
        File lib = libs[i];
        urls[i] = lib.toURI().toURL();
      } catch (Exception e) {
        log.warn("Failed adding library file to classpath: {}", lib, e);
      }
    }
    if (checkJars) checkJars(urls);
  }

  private void checkJars(URL[] urls) {
    try {
      Set<URL> classpath = JarUtil.parseClassPath();
      // check we don't have conflicting codebases
      Set<URL> intersection = new HashSet<>(classpath);
      Set<URL> pluginUrls = Sets.newHashSet(urls);
      intersection.retainAll(pluginUrls);
      if (!intersection.isEmpty()) {
        throw new IllegalStateException("Duplicate jars between plugin and host application: " + intersection);
      }
      // check we don't have conflicting classes
      Set<URL> union = new HashSet<>(classpath);
      union.addAll(pluginUrls);
      JarUtil.checkJars(union);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to load plugin " + getName() + " due to jar conflict", e);
    }
  }

  public void cancelUninstall() {
    if (uninstallFile.exists()) uninstallFile.delete();
  }

  public void prepareForUninstall() {
    try {
      if (!uninstallFile.exists()) uninstallFile.createNewFile();
    } catch (IOException e) {
      log.error("Failed to prepare plugin {} for removal", getName(), e);
    }
  }

  public void writeSiteProperties(String properties) throws IOException {
    String text = Strings.isNullOrEmpty(properties) ? "" : properties;
    Files.write(text, siteProperties, Charsets.UTF_8);
  }

  public String getSitePropertiesString() {
    if (!siteProperties.exists()) return "";
    try {
      return Files.toString(siteProperties, Charsets.UTF_8);
    } catch (IOException e) {
      log.error("Failed to read plugin site properties: {}", siteProperties.getAbsolutePath(), e);
      return "";
    }
  }
}
