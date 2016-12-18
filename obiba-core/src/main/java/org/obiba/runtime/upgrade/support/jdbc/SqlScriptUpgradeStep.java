/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.runtime.upgrade.support.jdbc;

import java.io.IOException;
import java.sql.SQLException;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.obiba.runtime.Version;
import org.obiba.runtime.jdbc.DatabaseProduct;
import org.obiba.runtime.jdbc.DatabaseProductRegistry;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class SqlScriptUpgradeStep extends AbstractUpgradeStep {

  private static final Logger log = LoggerFactory.getLogger(SqlScriptUpgradeStep.class);

  private DataSource dataSource;

  private Resource scriptPath;

  private String scriptBasename;

  private Resource script;

  public SqlScriptUpgradeStep() {
  }

  public SqlScriptUpgradeStep(DataSource dataSource, String scriptBasename, Resource scriptPath) {
    this.dataSource = dataSource;
    this.scriptBasename = scriptBasename;
    this.scriptPath = scriptPath;
  }

  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public void setScriptBasename(String scriptBasename) {
    this.scriptBasename = scriptBasename;
  }

  public void setScriptPath(Resource scriptPath) {
    this.scriptPath = scriptPath;
  }

  @PostConstruct
  public void initialize() throws IOException {
    log.debug("Identifying database.");
    DatabaseProduct product = getDatabaseProduct(dataSource);
    log.debug("Database product is: {}", product);
    script = scriptPath.createRelative(getProductSpecificScriptName(product));
    log.debug("Sql script {} exists {}", script.getDescription(), script.exists());
    if(!script.exists()) {
      script = scriptPath.createRelative(getScriptName());
      log.debug("Sql script {} exists {}", script.getDescription(), script.exists());
      if(!script.exists()) {
        throw new IllegalStateException(
            "Cannot find sql script to execute. Script path '" + scriptPath + "' basename '" + scriptBasename +
                "' database product '" + product + "'.");
      }
    }
  }

  @Override
  public void execute(Version currentVersion) {
    log.info("Applying script {} to database.", script.getFilename());
    executeScript(dataSource, script);
  }

  protected void executeScript(DataSource dataSource, Resource script) {
    try {
      ScriptUtils.executeSqlScript(dataSource.getConnection(), script);
    } catch(SQLException e) {
      log.info("Script execution failed {}.", e.getStackTrace());
    }
  }

  protected DatabaseProduct getDatabaseProduct(DataSource dataSource) {
    return new DatabaseProductRegistry().getDatabaseProduct(dataSource);
  }

  protected String getProductSpecificScriptName(DatabaseProduct product) {
    return scriptBasename + "_" + product.getNormalizedName() + ".sql";
  }

  protected String getScriptName() {
    return scriptBasename + ".sql";
  }

}
