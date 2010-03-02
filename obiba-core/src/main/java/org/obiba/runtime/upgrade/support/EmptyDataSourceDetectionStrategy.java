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
 * An implementation of {@link NewInstallationDetectionStrategy} that returns true if its {@code dataSource} attribute
 * contains no table.
 */
public class EmptyDataSourceDetectionStrategy implements NewInstallationDetectionStrategy {

  private static final Logger log = LoggerFactory.getLogger(EmptyDataSourceDetectionStrategy.class);

  private JdbcTemplate jdbcTemplate;

  public void setDataSource(DataSource dataSource) {
    this.jdbcTemplate = new JdbcTemplate(dataSource);
  }

  @Override
  public boolean isNewInstallation(VersionProvider runtimeVersionProvider) {
    Boolean result = (Boolean)jdbcTemplate.execute(new ConnectionCallback() {
      @Override
      public Object doInConnection(Connection con) throws SQLException, DataAccessException {
        String[] types = new String[] {"TABLE"};
        ResultSet tables = con.getMetaData().getTables(null, null, null, types);
        try {
          if(!tables.next()) {
            log.info("DataSource does not contain any table. New installation detected.");
            return Boolean.TRUE;
          }
        } finally {
          tables.close();
        }
        log.info("DataSource contains at least one table. This is not a new installation.");
        return Boolean.FALSE;
      }
    });
    return result;
  }

}
