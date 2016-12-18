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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;

import liquibase.change.AddNotNullConstraintChange;
import liquibase.change.Change;
import liquibase.change.DropColumnChange;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.sql.visitor.SqlVisitor;
import liquibase.database.structure.Column;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.exception.JDBCException;

public abstract class LiquiBaseUpgradeStep extends AbstractUpgradeStep {
  //
  // Instance Variables
  //

  private DataSource dataSource;

  //
  // AbstractUpgradeStep Methods
  //

  @Override
  public void execute(Version currentVersion) {
    applyChanges();
  }

  //
  // Methods
  //

  /**
   * Applies the schema changes specified by <code>getChanges()</code>.
   */
  public void applyChanges() {
    Database database = getDatabase();
    try {
      List<SqlVisitor> visitors = new ArrayList<SqlVisitor>();
      for(Change change : getChanges()) {
        change.executeStatements(database, visitors);
      }
    } catch(Exception ex) {
      throw new RuntimeException("Could not apply change to the database", ex);
    }
  }

  /**
   * Creates a "change" consisting of the adding of not-null constraints for the specified table column.
   *
   * @param databaseSnapshot database snapshot
   * @param table table
   * @param column column
   * @return not-null constraint change
   */
  public AddNotNullConstraintChange createAddNotNullConstraintChange(DatabaseSnapshot databaseSnapshot, String table,
      String column) {
    AddNotNullConstraintChange change = new AddNotNullConstraintChange();
    change.setTableName(table);
    change.setColumnName(column);

    // Set the column data type (required for MySQL and MSSQL).
    Column columnMetadata = databaseSnapshot.getColumn(table, column);
    String dataType = columnMetadata.getDataTypeString(databaseSnapshot.getDatabase());
    change.setColumnDataType(dataType);

    return change;
  }

  /**
   * Creates a "change" consisting of dropping the specified table column.
   *
   * @param databaseSnapshot database snapshot
   * @param table table
   * @param column column
   * @return drop column change
   */
  public DropColumnChange createDropColumnChange(DatabaseSnapshot databaseSnapshot, String table, String column) {
    DropColumnChange change = new DropColumnChange();
    change.setTableName(table);
    change.setColumnName(column);

    return change;
  }

  public Database getDatabase() {
    // Get a connection to the dataSource.
    Connection connection = null;
    try {
      connection = dataSource.getConnection();
    } catch(SQLException ex) {
      throw new RuntimeException("Could not acquire a connection to the datasource", ex);
    }

    // Find an appropriate database instance for the connection.
    Database database = null;
    try {
      DatabaseFactory databaseFactory = DatabaseFactory.getInstance();
      database = databaseFactory.findCorrectDatabaseImplementation(connection);
    } catch(JDBCException ex) {
      throw new RuntimeException("Could not find database implemention", ex);
    }

    return database;
  }

  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  /**
   * Returns the list of schema changes to be applied.
   *
   * @return schema changes to be applied
   */
  protected abstract List<Change> getChanges();
}