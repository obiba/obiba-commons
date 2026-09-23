/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.plugins;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class PluginsManagerHelperTest {

  private static final String PLUGIN = "mica-search-es8";

  private static final String SITE_DEFAULT = "transportAddresses=localhost:9200\n";

  private static final String SITE_USER = "transportAddresses=es.example.org:9200\n";

  private static final String YML_DEFAULT = "index:\n  max_result_window: 1000\n";

  private static final String YML_NEW_DEFAULT = "index:\n  max_result_window: 2000\n";

  private static final String YML_USER = "index:\n  max_result_window: 5000\n";

  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();

  private File pluginsDir;

  private File archiveDir;

  @Before
  public void setUp() throws IOException {
    pluginsDir = tmp.newFolder("plugins");
    archiveDir = new File(pluginsDir, ".archive");
  }

  @Test
  public void test_new_version_inherits_site_properties() throws IOException {
    install(PLUGIN, "2.0.2", defaults());
    write(PLUGIN, "2.0.2", PluginResources.SITE_PROPERTIES, SITE_USER);

    install(PLUGIN, "2.0.3", defaults());

    assertEquals(SITE_USER, read(PLUGIN, "2.0.3", PluginResources.SITE_PROPERTIES));
    // previous version is left unchanged
    assertEquals(SITE_USER, read(PLUGIN, "2.0.2", PluginResources.SITE_PROPERTIES));
  }

  @Test
  public void test_new_version_keeps_unmodified_yaml_default() throws IOException {
    install(PLUGIN, "2.0.2", defaults());

    install(PLUGIN, "2.0.3", ImmutableMap.of("elasticsearch.yml", YML_NEW_DEFAULT));

    assertEquals(YML_NEW_DEFAULT, read(PLUGIN, "2.0.3", "elasticsearch.yml"));
    assertFalse(file(PLUGIN, "2.0.3", "elasticsearch.yml" + PluginsManagerHelper.DIST_FILE_SUFFIX).exists());
  }

  @Test
  public void test_new_version_inherits_modified_yaml_and_keeps_new_default_as_dist() throws IOException {
    install(PLUGIN, "2.0.2", defaults());
    write(PLUGIN, "2.0.2", "elasticsearch.yml", YML_USER);

    install(PLUGIN, "2.0.3", ImmutableMap.of("elasticsearch.yml", YML_NEW_DEFAULT));

    assertEquals(YML_USER, read(PLUGIN, "2.0.3", "elasticsearch.yml"));
    assertEquals(YML_NEW_DEFAULT, read(PLUGIN, "2.0.3", "elasticsearch.yml" + PluginsManagerHelper.DIST_FILE_SUFFIX));
  }

  @Test
  public void test_new_version_inherits_modified_yaml_without_dist_when_default_is_same() throws IOException {
    install(PLUGIN, "2.0.2", defaults());
    write(PLUGIN, "2.0.2", "elasticsearch.yml", YML_USER);

    install(PLUGIN, "2.0.3", defaults());

    assertEquals(YML_USER, read(PLUGIN, "2.0.3", "elasticsearch.yml"));
    // default did not change between versions, nothing to review
    assertFalse(file(PLUGIN, "2.0.3", "elasticsearch.yml" + PluginsManagerHelper.DIST_FILE_SUFFIX).exists());
  }

  @Test
  public void test_new_version_inherits_yaml_when_previous_archive_is_missing() throws IOException {
    // previous version installed manually (no archived zip)
    installFolder(PLUGIN, "2.0.2", ImmutableMap.of("elasticsearch.yml", YML_DEFAULT));

    install(PLUGIN, "2.0.3", ImmutableMap.of("elasticsearch.yml", YML_NEW_DEFAULT));

    assertEquals(YML_DEFAULT, read(PLUGIN, "2.0.3", "elasticsearch.yml"));
    assertEquals(YML_NEW_DEFAULT, read(PLUGIN, "2.0.3", "elasticsearch.yml" + PluginsManagerHelper.DIST_FILE_SUFFIX));
  }

  @Test
  public void test_new_version_inherits_from_most_recent_previous_version() throws IOException {
    install(PLUGIN, "2.0.1", defaults());
    write(PLUGIN, "2.0.1", PluginResources.SITE_PROPERTIES, "transportAddresses=old:9200\n");
    install(PLUGIN, "2.0.10", defaults());
    write(PLUGIN, "2.0.10", PluginResources.SITE_PROPERTIES, SITE_USER);

    install(PLUGIN, "2.1.0", defaults());

    assertEquals(SITE_USER, read(PLUGIN, "2.1.0", PluginResources.SITE_PROPERTIES));
  }

  @Test
  public void test_new_major_version_does_not_inherit() throws IOException {
    install(PLUGIN, "2.0.2", defaults());
    write(PLUGIN, "2.0.2", PluginResources.SITE_PROPERTIES, SITE_USER);
    write(PLUGIN, "2.0.2", "elasticsearch.yml", YML_USER);

    install(PLUGIN, "3.0.0", defaults());

    assertEquals(SITE_DEFAULT, read(PLUGIN, "3.0.0", PluginResources.SITE_PROPERTIES));
    assertEquals(YML_DEFAULT, read(PLUGIN, "3.0.0", "elasticsearch.yml"));
  }

  @Test
  public void test_other_plugin_does_not_inherit() throws IOException {
    install(PLUGIN, "2.0.2", defaults());
    write(PLUGIN, "2.0.2", PluginResources.SITE_PROPERTIES, SITE_USER);

    install("mica-search-es9", "2.0.3", defaults());

    assertEquals(SITE_DEFAULT, read("mica-search-es9", "2.0.3", PluginResources.SITE_PROPERTIES));
  }

  @Test
  public void test_older_version_does_not_inherit_from_newer() throws IOException {
    install(PLUGIN, "2.0.3", defaults());
    write(PLUGIN, "2.0.3", PluginResources.SITE_PROPERTIES, SITE_USER);

    install(PLUGIN, "2.0.2", defaults());

    assertEquals(SITE_DEFAULT, read(PLUGIN, "2.0.2", PluginResources.SITE_PROPERTIES));
  }

  @Test
  public void test_new_version_does_not_inherit_from_plugin_to_uninstall() throws IOException {
    install(PLUGIN, "2.0.2", defaults());
    write(PLUGIN, "2.0.2", PluginResources.SITE_PROPERTIES, SITE_USER);
    write(PLUGIN, "2.0.2", PluginResources.UNINSTALL_FILE, "");

    install(PLUGIN, "2.0.3", defaults());

    assertEquals(SITE_DEFAULT, read(PLUGIN, "2.0.3", PluginResources.SITE_PROPERTIES));
  }

  @Test
  public void test_reinstall_same_version_keeps_site_properties() throws IOException {
    install(PLUGIN, "2.0.2", defaults());
    write(PLUGIN, "2.0.2", PluginResources.SITE_PROPERTIES, SITE_USER);

    install(PLUGIN, "2.0.2", defaults());

    assertEquals(SITE_USER, read(PLUGIN, "2.0.2", PluginResources.SITE_PROPERTIES));
  }

  //
  // Private methods
  //

  private static Map<String, String> defaults() {
    return ImmutableMap.of("elasticsearch.yml", YML_DEFAULT);
  }

  /**
   * Drop a plugin distribution zip in the plugins folder and prepare the plugins, as the host application does.
   */
  private void install(String name, String version, Map<String, String> files) throws IOException {
    String folder = name + "-" + version;
    File zip = new File(pluginsDir, folder + PluginResources.PLUGIN_DIST_SUFFIX);
    try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip))) {
      out.putNextEntry(new ZipEntry(folder + "/"));
      out.closeEntry();
      out.putNextEntry(new ZipEntry(folder + "/lib/"));
      out.closeEntry();
      for (Map.Entry<String, String> entry : distFiles(name, version, files).entrySet()) {
        out.putNextEntry(new ZipEntry(folder + "/" + entry.getKey()));
        out.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
        out.closeEntry();
      }
    }
    PluginsManagerHelper.preparePlugins(pluginsDir, archiveDir);
  }

  /**
   * Create a plugin folder directly, without distribution zip.
   */
  private void installFolder(String name, String version, Map<String, String> files) throws IOException {
    File lib = file(name, version, "lib");
    lib.mkdirs();
    for (Map.Entry<String, String> entry : distFiles(name, version, files).entrySet()) {
      write(name, version, entry.getKey(), entry.getValue());
    }
  }

  private static Map<String, String> distFiles(String name, String version, Map<String, String> files) {
    return ImmutableMap.<String, String>builder()
        .put(PluginResources.PLUGIN_PROPERTIES, "name=" + name + "\nversion=" + version + "\n")
        .put(PluginResources.SITE_PROPERTIES, SITE_DEFAULT)
        .putAll(files)
        .build();
  }

  private File file(String name, String version, String fileName) {
    return new File(new File(pluginsDir, name + "-" + version), fileName);
  }

  private void write(String name, String version, String fileName, String content) throws IOException {
    Files.writeString(file(name, version, fileName).toPath(), content, StandardCharsets.UTF_8);
  }

  private String read(String name, String version, String fileName) throws IOException {
    return Files.readString(file(name, version, fileName).toPath(), StandardCharsets.UTF_8);
  }

}
