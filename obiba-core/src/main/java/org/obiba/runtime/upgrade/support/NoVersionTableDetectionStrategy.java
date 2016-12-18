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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.obiba.runtime.upgrade.VersionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * An implementation of {@link org.obiba.runtime.upgrade.support.NewInstallationDetectionStrategy} that returns true if its {@code dataSource} attribute
 * contains no version table.
 * See {@link VersionTableInstallStep}
 */
public class NoVersionTableDetectionStrategy implements NewInstallationDetectionStrategy {

  private static final Logger log = LoggerFactory.getLogger(NoVersionTableDetectionStrategy.class);

  private JdbcTemplate jdbcTemplate;

  public void setDataSource(DataSource dataSource) {
    jdbcTemplate = new JdbcTemplate(dataSource);
  }

  @Override
  public boolean isNewInstallation(VersionProvider runtimeVersionProvider) {
    return jdbcTemplate.execute(new ConnectionCallback<Boolean>() {
      @Override
      public Boolean doInConnection(Connection con) throws SQLException, DataAccessException {
        String[] types = new String[] { "TABLE" };
        ResultSet tables = con.getMetaData().getTables(null, null, null, types);
        try {
          while(tables.next()) {
            if(VersionTableInstallStep.TABLE_NAME.equalsIgnoreCase(tables.getString("TABLE_NAME"))) {
              log.info("DataSource contains '" + VersionTableInstallStep.TABLE_NAME +
                  "' table. This is not a new installation.");
              return false;
            }
          }
          log.info("DataSource does not contain '" + VersionTableInstallStep.TABLE_NAME +
              "' table. New installation detected.");
          return true;
        } finally {
          tables.close();
        }
      }
    });
  }

}
