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

import com.google.common.io.Files;
import org.obiba.core.util.FileUtil;
import org.obiba.plugins.spi.ServicePlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class PluginsManagerHelper {

  private static final Logger log = LoggerFactory.getLogger(PluginsManagerHelper.class);

  private static final SimpleDateFormat ISO_8601 = new SimpleDateFormat("yyyy-MM-dd");

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
        extractPlugin(child);
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
   * @throws IOException
   */
  private static void extractPlugin(File fileZip) throws IOException {
    File destination = new File(fileZip.getParent());
    File expectedFolder = new File(destination, fileZip.getName().replace(PluginResources.PLUGIN_DIST_SUFFIX, ""));
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

}
