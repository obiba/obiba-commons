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

import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import org.obiba.core.util.FileUtil;
import org.obiba.plugins.spi.ServicePlugin;
import org.obiba.runtime.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class PluginsManagerHelper {

  private static final Logger log = LoggerFactory.getLogger(PluginsManagerHelper.class);

  private static final SimpleDateFormat ISO_8601 = new SimpleDateFormat("yyyy-MM-dd");

  public static final String DIST_FILE_SUFFIX = ".dist";

  /**
   * Uncompress and archive any zip file that could be found.
   *
   * @param pluginsDir
   */
  public static void preparePlugins(File pluginsDir, File archiveDir) {
    File[] children = pluginsDir.listFiles(pathname -> !pathname.isDirectory() && pathname.getName().endsWith(PluginResources.PLUGIN_DIST_SUFFIX));
    if (children == null || children.length == 0) return;
    if (!archiveDir.exists()) archiveDir.mkdirs();
    for (File child : children) {
      try {
        extractPlugin(child, archiveDir);
        Files.move(child, new File(archiveDir, child.getName()));
      } catch (IOException e) {
        log.warn("Failed extracting plugin file: {}" + child.getAbsolutePath(), e);
      }
    }
  }

  /**
   * Add plugin if valid and if most recent version or archive it if marked for uninstallation.
   *
   * @param pluginsMap
   * @param plugin
   * @param archiveDir
   */
  public static void processPlugin(Map<String, PluginResources> pluginsMap, PluginResources plugin, File archiveDir) {
    if (plugin.isToUninstall()) {
      File archiveDest = new File(archiveDir, plugin.getDirectory().getName() + "-" + ISO_8601.format(new Date()));
      log.info("Archiving plugin {} to {}", plugin.getName(), archiveDest.getAbsolutePath());
      try {
        if (archiveDest.exists()) FileUtil.delete(archiveDest);
        archiveDir.mkdirs();
        FileUtil.moveFile(plugin.getDirectory(), archiveDest);
      } catch (IOException e) {
        log.info("Failed to archive plugin directory: {}", plugin.getDirectory().getName(), e);
      }
      return;
    }
    if (!plugin.isValid()) return;
    if (!pluginsMap.containsKey(plugin.getName()))
      pluginsMap.put(plugin.getName(), plugin);
    else if (plugin.getVersion().compareTo(pluginsMap.get(plugin.getName()).getVersion()) > 0)
      pluginsMap.put(plugin.getName(), plugin);
  }


  /**
   * Register every instance of service plugin.
   *
   * @param servicePlugins
   * @param pluginsMap
   * @param service
   */
  public static void registerServicePlugin(List<ServicePlugin> servicePlugins, Map<String, PluginResources> pluginsMap, ServicePlugin service) {
    try {
      PluginResources plugin = pluginsMap.get(service.getName());
      service.configure(plugin.getProperties());
      service.start();
      servicePlugins.add(service);
    } catch (Exception e) {
      log.warn("Error initializing/starting plugin service: {}", service.getClass(), e);
    }
  }

  /**
   * Register only the first service plugin of a given type.
   *
   * @param servicePlugins
   * @param pluginsMap
   * @param service
   */
  public static void registerSingletonServicePlugin(List<ServicePlugin> servicePlugins, Map<String, PluginResources> pluginsMap, ServicePlugin service) {
    PluginResources plugin = pluginsMap.get(service.getName());
    // check if service plugin of same type is already registered
    for (ServicePlugin servicePlugin : servicePlugins) {
      PluginResources p = pluginsMap.get(servicePlugin.getName());
      if (p.getType().equals(plugin.getType())) return;
    }
    registerServicePlugin(servicePlugins, pluginsMap, service);
  }

  /**
   * Extract plugin folder from zip file.
   *
   * @param fileZip
   * @param archiveDir
   * @throws IOException
   */
  private static void extractPlugin(File fileZip, File archiveDir) throws IOException {
    File destination = new File(fileZip.getParent());
    File expectedFolder = new File(destination, fileZip.getName().replace(PluginResources.PLUGIN_DIST_SUFFIX, ""));
    boolean reinstall = expectedFolder.exists();
    // backup any site properties
    File sitePropertiesBackup = backupPluginSiteProperties(expectedFolder);
    // Open the zip file
    ZipFile zipFile = new ZipFile(fileZip);
    Enumeration<?> enu = zipFile.entries();
    while (enu.hasMoreElements()) {
      ZipEntry zipEntry = (ZipEntry) enu.nextElement();
      String name = zipEntry.getName();
      log.info("Plugin extract: {}", name);
      // Do we need to create a directory ?
      File file = new File(destination, name);
      if(!file.toPath().normalize().startsWith(destination.toPath().normalize())) {
        throw new IOException("Bad zip entry");
      }
      if (name.endsWith("/")) {
        file.mkdirs();
        continue;
      }
      // Extract the file
      InputStream is = zipFile.getInputStream(zipEntry);
      FileOutputStream fos = new FileOutputStream(file);
      byte[] bytes = new byte[1024];
      int length;
      while ((length = is.read(bytes)) >= 0) {
        fos.write(bytes, 0, length);
      }
      is.close();
      fos.close();
    }
    zipFile.close();
    // restore site properties
    restorePluginSiteProperties(expectedFolder, sitePropertiesBackup);
    // new version of the plugin: inherit settings from the previous version
    if (!reinstall) inheritPluginSettings(expectedFolder, archiveDir);
  }

  /**
   * Backup site properties file if found and clear old plugin folder.
   *
   * @param pluginFolder
   * @return
   * @throws IOException
   */
  private static File backupPluginSiteProperties(File pluginFolder) throws IOException {
    File sitePropertiesBackup = null;
    if (pluginFolder.exists()) {
      File siteProperties = new File(pluginFolder, PluginResources.SITE_PROPERTIES);
      sitePropertiesBackup = File.createTempFile("site", ".properties");
      if (siteProperties.exists()) {
        FileUtil.copyFile(siteProperties, sitePropertiesBackup);
      }
      FileUtil.delete(pluginFolder);
    }
    return sitePropertiesBackup;
  }

  /**
   * Restore any site properties file that would have been backed up.
   *
   * @param pluginFolder
   * @param sitePropertiesBackup
   * @throws IOException
   */
  private static void restorePluginSiteProperties(File pluginFolder, File sitePropertiesBackup) throws IOException {
    if (sitePropertiesBackup == null || !sitePropertiesBackup.exists()) return;
    File siteProperties = new File(pluginFolder, PluginResources.SITE_PROPERTIES);
    if (siteProperties.exists()) FileUtil.delete(siteProperties);
    FileUtil.copyFile(sitePropertiesBackup, siteProperties);
    sitePropertiesBackup.delete();
  }

  /**
   * Copy the settings of the most recent previous version of the same plugin (same name and same major version)
   * into the folder of a newly installed version: the site properties and the yaml files modified by the user.
   * The previous plugin folder is left unchanged.
   *
   * @param pluginFolder
   * @param archiveDir
   */
  private static void inheritPluginSettings(File pluginFolder, File archiveDir) {
    Properties properties = readPluginProperties(pluginFolder);
    if (properties == null) return;
    String name = properties.getProperty("name");
    Version version = parseVersion(properties.getProperty("version"));
    if (Strings.isNullOrEmpty(name) || version == null) return;
    File previousFolder = findPreviousPluginFolder(pluginFolder, name, version);
    if (previousFolder == null) {
      log.info("Plugin {} {}: no previous version with same major version to inherit settings from", name, version);
      return;
    }
    try {
      inheritSiteProperties(pluginFolder, previousFolder);
      inheritYamlFiles(pluginFolder, previousFolder, archiveDir);
    } catch (IOException e) {
      log.warn("Plugin {} {}: failed to inherit settings from {}", name, version, previousFolder.getAbsolutePath(), e);
    }
  }

  /**
   * Find the most recent folder of a plugin with the same name and major version, older than the given version,
   * and not marked for uninstallation.
   *
   * @param pluginFolder
   * @param name
   * @param version
   * @return null if not found
   */
  private static File findPreviousPluginFolder(File pluginFolder, String name, Version version) {
    File[] siblings = pluginFolder.getParentFile().listFiles(pathname -> pathname.isDirectory()
        && !pathname.getName().startsWith(".")
        && !pathname.equals(pluginFolder)
        && !new File(pathname, PluginResources.UNINSTALL_FILE).exists());
    if (siblings == null) return null;
    File previousFolder = null;
    Version previousVersion = null;
    for (File sibling : siblings) {
      Properties properties = readPluginProperties(sibling);
      if (properties == null || !name.equals(properties.getProperty("name"))) continue;
      Version siblingVersion = parseVersion(properties.getProperty("version"));
      if (siblingVersion == null
          || siblingVersion.getMajor() != version.getMajor()
          || siblingVersion.compareTo(version) >= 0) continue;
      if (previousVersion == null || siblingVersion.compareTo(previousVersion) > 0) {
        previousFolder = sibling;
        previousVersion = siblingVersion;
      }
    }
    return previousFolder;
  }

  /**
   * Site properties are always user settings: copy them over the default ones.
   *
   * @param pluginFolder
   * @param previousFolder
   * @throws IOException
   */
  private static void inheritSiteProperties(File pluginFolder, File previousFolder) throws IOException {
    File previousSiteProperties = new File(previousFolder, PluginResources.SITE_PROPERTIES);
    if (!previousSiteProperties.exists()) return;
    Files.copy(previousSiteProperties, new File(pluginFolder, PluginResources.SITE_PROPERTIES));
    log.info("Plugin {}: inherited {} from {}", pluginFolder.getName(), PluginResources.SITE_PROPERTIES, previousFolder.getName());
  }

  /**
   * Yaml files hold user settings as well as plugin defaults: copy only the ones that were modified by the user
   * (or that cannot be verified against the previous plugin archive) and keep the new default as a ".dist" file.
   *
   * @param pluginFolder
   * @param previousFolder
   * @param archiveDir
   * @throws IOException
   */
  private static void inheritYamlFiles(File pluginFolder, File previousFolder, File archiveDir) throws IOException {
    File[] previousFiles = previousFolder.listFiles(pathname -> pathname.isFile() && isYamlFile(pathname.getName()));
    if (previousFiles == null) return;
    File previousZip = new File(archiveDir, previousFolder.getName() + PluginResources.PLUGIN_DIST_SUFFIX);
    for (File previousFile : previousFiles) {
      byte[] content = Files.toByteArray(previousFile);
      byte[] original = readZipEntry(previousZip, previousFolder.getName() + "/" + previousFile.getName());
      if (original != null && Arrays.equals(original, content)) continue; // not modified by the user
      File file = new File(pluginFolder, previousFile.getName());
      byte[] newDefault = file.exists() ? Files.toByteArray(file) : null;
      if (newDefault != null && Arrays.equals(newDefault, content)) continue;
      if (newDefault != null && (original == null || !Arrays.equals(newDefault, original))) {
        // the default has changed (or cannot be verified): keep it for review
        File distFile = new File(pluginFolder, file.getName() + DIST_FILE_SUFFIX);
        Files.copy(file, distFile);
        log.warn("Plugin {}: inherited {} from {}, the new default was saved as {}, please review",
            pluginFolder.getName(), file.getName(), previousFolder.getName(), distFile.getName());
      } else {
        log.info("Plugin {}: inherited {} from {}", pluginFolder.getName(), file.getName(), previousFolder.getName());
      }
      Files.copy(previousFile, file);
    }
  }

  private static boolean isYamlFile(String fileName) {
    return fileName.endsWith(".yml") || fileName.endsWith(".yaml");
  }

  /**
   * Read the content of a zip entry.
   *
   * @param fileZip
   * @param entryName
   * @return null if the zip file or the entry does not exist or cannot be read
   */
  private static byte[] readZipEntry(File fileZip, String entryName) {
    if (!fileZip.exists()) return null;
    try (ZipFile zipFile = new ZipFile(fileZip)) {
      ZipEntry zipEntry = zipFile.getEntry(entryName);
      if (zipEntry == null) return null;
      try (InputStream is = zipFile.getInputStream(zipEntry)) {
        return ByteStreams.toByteArray(is);
      }
    } catch (IOException e) {
      log.warn("Failed reading {} from plugin archive: {}", entryName, fileZip.getAbsolutePath(), e);
      return null;
    }
  }

  private static Properties readPluginProperties(File pluginFolder) {
    File propertiesFile = new File(pluginFolder, PluginResources.PLUGIN_PROPERTIES);
    if (!propertiesFile.exists()) return null;
    try (FileInputStream in = new FileInputStream(propertiesFile)) {
      Properties properties = new Properties();
      properties.load(in);
      return properties;
    } catch (IOException e) {
      log.warn("Failed reading plugin properties: {}", propertiesFile.getAbsolutePath(), e);
      return null;
    }
  }

  private static Version parseVersion(String version) {
    if (Strings.isNullOrEmpty(version)) return null;
    try {
      return new Version(version);
    } catch (RuntimeException e) {
      return null;
    }
  }

}
