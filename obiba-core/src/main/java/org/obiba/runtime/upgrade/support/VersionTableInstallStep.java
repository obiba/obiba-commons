package org.obiba.runtime.upgrade.support;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.InstallStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.CreateTableChange;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.sql.visitor.SqlVisitor;
import liquibase.exception.JDBCException;

@SuppressWarnings("UnusedDeclaration")
public class VersionTableInstallStep implements InstallStep {

  private static final Logger log = LoggerFactory.getLogger(VersionTableInstallStep.class);

  private String description = "Create version table.";

  private JdbcTemplate jdbcTemplate;

  public void setDataSource(DataSource dataSource) {
    jdbcTemplate = new JdbcTemplate(dataSource);
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public String getDescription() {
    return description;
  }

  /**
   * Creates the <code>version</code> table used to keep track of the currently installed version.
   * <p/>
   * This method is called in the case of a new installation (in which the table would be missing).
   */
  @Override
  public void execute(Version currentVersion) {
    log.info("Creating version table...");

    jdbcTemplate.execute(new ConnectionCallback<Object>() {
      @Override
      public Object doInConnection(Connection connection) throws SQLException, DataAccessException {

        // Find an appropriate database instance for the connection.
        Database database = null;
        try {
          DatabaseFactory databaseFactory = DatabaseFactory.getInstance();
          database = databaseFactory.findCorrectDatabaseImplementation(connection);
        } catch(JDBCException ex) {
          throw new RuntimeException("Could not create version table", ex);
        }
        // Create the version table.
        try {
          List<SqlVisitor> sqlVisitors = new ArrayList<SqlVisitor>();
          sqlVisitors.add(new CreateInnoDBTableSqlVisitor());
          doBuildCreateVersionTableChange().executeStatements(database, sqlVisitors);
          log.info("Successfully created the version table.");
        } catch(Exception ex) {
          throw new RuntimeException("Could not create version table", ex);
        }
        return null;
      }

      private CreateTableChange doBuildCreateVersionTableChange() {
        CreateTableChange createVersionTable = new CreateTableChange();
        createVersionTable.setTableName("version");
        createVersionTable.addColumn(createColumn("major", "java.sql.Types.INTEGER", false));
        createVersionTable.addColumn(createColumn("minor", "java.sql.Types.INTEGER", false));
        createVersionTable.addColumn(createColumn("micro", "java.sql.Types.INTEGER", false));
        createVersionTable.addColumn(createColumn("qualifier", "java.sql.Types.VARCHAR(50)", true));
        createVersionTable.addColumn(createColumn("version_string", "java.sql.Types.VARCHAR(50)", false));
        return createVersionTable;
      }

      private ColumnConfig createColumn(String columnName, String columnType, boolean nullable) {
        ColumnConfig column = new ColumnConfig();
        column.setName(columnName);
        column.setType(columnType);

        ConstraintsConfig constraints = new ConstraintsConfig();
        constraints.setNullable(nullable);
        column.setConstraints(constraints);

        return column;
      }

    });

  }

  private static class CreateInnoDBTableSqlVisitor implements SqlVisitor {

    @Override
    public String getTagName() {
      return "createInnoDBTableSqlVisitor";
    }

    @Override
    public void setApplicableDbms(@SuppressWarnings("rawtypes") Collection applicableDbms) {
      // no-op
    }

    @Override
    public boolean isApplicable(Database database) {
      return "mysql".equals(database.getTypeName());
    }

    @Override
    @SuppressWarnings({ "AssignmentToMethodParameter", "PMD.AvoidReassigningParameters" })
    public String modifySql(String sql, Database database) {
      if(sql.toUpperCase().startsWith("CREATE TABLE")) {
        sql += " ENGINE=InnoDB";
      }
      return sql;
    }
  }
}
