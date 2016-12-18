/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.runtime.upgrade.support;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.VersionModifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

public class JdbcVersionModifier implements VersionModifier, InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(JdbcVersionModifier.class);

  private DataSource datasource;

  private JdbcTemplate jdbcTemplate;

  private Version version;

  @Override
  public Version getVersion() {
    return version;
  }

  @Override
  public void setVersion(Version version) {
    this.version = version;
    jdbcTemplate.execute("delete from version");
    jdbcTemplate.update("insert into version ( major, minor, micro, qualifier, version_string ) values (?,?,?,?,?)",
        version.getMajor(), version.getMinor(), version.getMicro(), version.getQualifier(), version.toString());
  }

  public void setDatasource(DataSource datasource) {
    this.datasource = datasource;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    jdbcTemplate = new JdbcTemplate(datasource);

    try {
      log.info("Attempting to retrieve the currently running version information from the database...");
      List<Map<String, Object>> result = jdbcTemplate.queryForList("select * from version");
      if(result != null && result.size() > 0) {
        Map<String, Object> versionInfo = result.get(0);
        String major = versionInfo.get("major").toString();
        String minor = versionInfo.get("minor").toString();
        String micro = versionInfo.get("micro").toString();
        String qualifier = versionInfo.get("qualifier").toString();
        setVersion(new Version(Integer.parseInt(major), Integer.parseInt(minor), Integer.parseInt(micro), qualifier));
        log.info("The current version is {} ", version);
      }
    } catch(DataAccessException e) {
      log.info("Could not retrieve the current version. This looks like a new installation, so no upgrade is needed.");
    }

  }
}
