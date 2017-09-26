/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.plugins;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.VersionProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Cache to not query for update site at each request while managing the plugins.
 */
public class PluginRepositoryCache {
  private static final String REPO_FILE = "plugins.json";
  private static final int DELAY = 600;
  private PluginRepository pluginRepository;
  private long lastUpdate;
  private final VersionProvider hostVersionProvider;
  private final String updateSite;

  public PluginRepositoryCache(VersionProvider hostVersionProvider, String updateSite) {
    this.hostVersionProvider = hostVersionProvider;
    this.updateSite = updateSite;
  }

  public PluginRepository getOrUpdatePluginRepository() {
    if (hasExpired()) initializePluginRepository();
    return pluginRepository;
  }

  private boolean hasExpired() {
    return pluginRepository == null || (nowInSeconds() - lastUpdate) > DELAY;
  }

  public Date getLastUpdate() {
    return lastUpdate == 0 ? null : new Date(lastUpdate * 1000);
  }

  public File downloadPlugin(String name, String version, File tmpDir) throws IOException {
    Version versionObj = new Version(version);
    Optional<PluginPackage> pluginPackage = getOrUpdatePluginRepository().getPlugins().stream().filter(pp -> pp.isSameAs(name, versionObj)).findFirst();
    if (!pluginPackage.isPresent())
      throw new NoSuchElementException("Plugin " + name + ":" + version + " cannot be found");
    File pluginFile = new File(tmpDir, pluginPackage.get().getFileName());
    ReadableByteChannel rbc = Channels.newChannel(getRepositoryURL(pluginFile.getName()).openStream());
    FileOutputStream fos = new FileOutputStream(pluginFile);
    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
    return pluginFile;
  }


  private void initializePluginRepository() {
    ObjectMapper mapper = new ObjectMapper();
    try {
      pluginRepository = mapper.readValue(getRepositoryURL(REPO_FILE), new TypeReference<PluginRepository>() {
      });
      lastUpdate = nowInSeconds();
    } catch (Exception e) {
      throw new PluginRepositoryException("Cannot update plugin site: " + e.getMessage(), e);
    }
  }

  private long nowInSeconds() {
    return new Date().getTime() / 1000;
  }

  private URL getRepositoryURL(String fileName) throws MalformedURLException {
    String basePath = updateSite.endsWith("/") ? updateSite : updateSite + "/";
    return new URL(basePath + "/" + fileName);
  }

  public String getPluginLatestVersion(String name) {
    Version version = new Version("0.0.0");
    for (PluginPackage pp : getOrUpdatePluginRepository().getPlugins().stream()
        .filter(pp -> pp.getName().equals(name))
        .filter(pp -> hostVersionProvider.getVersion().compareTo(pp.getOpalVersion()) >= 0)
        .collect(Collectors.toList())) {
      if (pp.getVersion().compareTo(version) > 0) version = pp.getVersion();
    }
    return version.toString();
  }
}
