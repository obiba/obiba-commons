/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.runtime.upgrade.support;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.jdbc.support.DatabaseMetaDataCallback;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;

public class DatabaseMetadataUtil {
  //
  // Instance Variables
  //

  private final DataSource dataSource;

  //
  // Constructors
  //

  public DatabaseMetadataUtil(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  //
  // Methods
  //

  /**
   * Returns the database product name.
   *
   * @return database product name
   */
  public String getDatabaseProductName() {
    String databaseProductName;
    try {
      databaseProductName = (String) JdbcUtils.extractDatabaseMetaData(dataSource, new DatabaseMetaDataCallback() {
        @Override
        public Object processMetaData(DatabaseMetaData dbmd) throws SQLException, MetaDataAccessException {
          return dbmd.getDatabaseProductName();
        }
      });
    } catch(MetaDataAccessException e) {
      throw new RuntimeException(e);
    }

    return databaseProductName;
  }

  /**
   * Indicates whether the specified table exists.
   *
   * @param tableName the table name
   * @return <code>true</code> if the table exists
   */
  public boolean isTableExists(final String tableName) {
    boolean tablePresent = false;

    try {
      tablePresent = (Boolean) JdbcUtils.extractDatabaseMetaData(dataSource, new DatabaseMetaDataCallback() {
        @Override
        public Object processMetaData(DatabaseMetaData dbmd) throws SQLException, MetaDataAccessException {
          return dbmd.getTables(null, null, tableName, null).next();
        }
      });
    } catch(MetaDataAccessException ex) {
      throw new RuntimeException(ex);
    }

    return tablePresent;
  }

  /**
   * Indicates whether the specified table contains the specified column.
   *
   * @param tableName the table name
   * @param columnName the column name
   * @return <code>true</code> if the column exists in the table
   */
  public boolean hasColumn(final String tableName, final String columnName) {
    boolean columnPresent = false;

    try {
      columnPresent = (Boolean) JdbcUtils.extractDatabaseMetaData(dataSource, new DatabaseMetaDataCallback() {
        @Override
        public Object processMetaData(DatabaseMetaData dbmd) throws SQLException, MetaDataAccessException {
          return dbmd.getColumns(null, null, tableName, columnName).next();
        }
      });
    } catch(MetaDataAccessException ex) {
      throw new RuntimeException(ex);
    }

    return columnPresent;
  }
}
