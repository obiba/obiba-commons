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
 * contains no table.
 */
public class VersionTableDetectionStrategy implements NewInstallationDetectionStrategy {

  private static final Logger log = LoggerFactory.getLogger(VersionTableDetectionStrategy.class);

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
          boolean found = false;
          if(!tables.next()) {
            log.info("DataSource does not contain any table. New installation detected.");
            return true;
          }
          log.info("DataSource contains at least one table. Looking for version table...");
          tables.first();
          while(!found && tables.next()) {
            String tableName = tables.getString("TABLE_NAME");
            log.info("Existing table: {}", tableName);
            found = tableName.equalsIgnoreCase(VersionTableInstallStep.TABLE_NAME);
          }
          return !found;
        } finally {
          tables.close();
        }
      }
    });
  }

}
