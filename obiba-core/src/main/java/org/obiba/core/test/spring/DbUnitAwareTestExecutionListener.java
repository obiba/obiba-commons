package org.obiba.core.test.spring;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.AnnotatedElement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.ext.hsqldb.HsqldbDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * TestExecutionListener implementation that handles the {@link Dataset} annotation.
 * <p/>
 * Using this listener (through {@link TestExecutionListeners} allows seeding a test
 * database before executing unit test methods.
 */
public class DbUnitAwareTestExecutionListener extends AbstractTestExecutionListener {

  private static final Logger log = LoggerFactory.getLogger(DbUnitAwareTestExecutionListener.class);

  @Override
  public void afterTestMethod(TestContext context) throws Exception {
    log.debug("{}.afterTestMethod() for context {}", getClass().getSimpleName(), context);
    handleElement(context, context.getTestMethod(), false);
  }

  @Override
  public void beforeTestMethod(TestContext context) throws Exception {
    log.debug("{}.beforeTestMethod() for context {}", getClass().getSimpleName(), context);
    handleElement(context, context.getTestMethod(), true);
  }

  @Override
  public void prepareTestInstance(TestContext context) throws Exception {
    log.debug("{}.prepareTestInstance() for context {}", getClass().getSimpleName(), context);
    if(context.getAttribute("dbUnit" + context.getTestClass()) == null) {
      handleElement(context, context.getTestClass(), true);
      context.setAttribute("dbUnit" + context.getTestClass(), new Object());
    }
  }

  private void handleElement(TestContext context, AnnotatedElement element, boolean before) throws Exception {
    Datasets ds = element.getAnnotation(Datasets.class);
    if(ds != null) {
      for(Dataset dataset : ds.value()) {
        handleAnnotation(context, dataset, before);
      }
    } else {
      Dataset da = element.getAnnotation(Dataset.class);
      if(da != null) {
        handleAnnotation(context, da, before);
      } else {
        log.debug("No {} annotation found on element {}.", Dataset.class.getSimpleName(), element);
      }
    }
  }

  private void handleAnnotation(TestContext context, Dataset datasetAnnotation, boolean before) throws Exception {
    log.debug("Handling annotation {}", datasetAnnotation);

    String className = context.getTestClass().getSimpleName();
    String dataSourceBeanName = datasetAnnotation.dataSourceBean();

    DataSource dataSource = (DataSource) context.getApplicationContext().getBean(dataSourceBeanName);
    DatabaseDataSourceConnection connection = new DatabaseDataSourceConnection(dataSource);
    connection.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new HsqldbDataTypeFactory());
    try {
      String filenames[] = datasetAnnotation.filenames();
      if(filenames == null || filenames.length == 0) {
        filenames = new String[] { className + ".xml" };
      }
      for(String filename : filenames) {
        seedDatabase(context, datasetAnnotation, before, className, connection, filename);
      }
    } finally {
      try {
        connection.close();
      } catch(SQLException e) {
        // Ignore so we don't hide the pertinent exception if any...
      }
    }
  }

  private void seedDatabase(TestContext context, Dataset datasetAnnotation, boolean before, String className,
      IDatabaseConnection connection, String filename) throws IOException, SQLException, DatabaseUnitException {
    log.debug("Seeding database with dataset {}.", filename);
    InputStream is = context.getTestClass().getResourceAsStream(filename);
    if(is == null) {
      log.error("Test case {}: cannot find resource {}.", className, filename);
    } else {
      IDataSet dataset = new FlatXmlDataSet(is);
      try {
        getDbUnitOp(before ? datasetAnnotation.beforeOperation() : datasetAnnotation.afterOperation())
            .execute(connection, dataset);
      } catch(DatabaseUnitException e) {
        log.error("Exception while inserting dataset filename {} for test case {}",
            new String[] { filename, className, e.getMessage() });
        throw e;
      }
    }
  }

  /**
   * Converts a {@link DatasetOperationType} into a DbUnit {@link DatabaseOperation}
   *
   * @param type the dataset type
   * @return the corresponding {@link DatabaseOperation}
   */
  @SuppressWarnings("PMD.NcssMethodCount")
  private DatabaseOperation getDbUnitOp(DatasetOperationType type) {
    switch(type) {
      case CLEAN_INSERT:
        return DatabaseOperation.CLEAN_INSERT;
      case DELETE:
        return DatabaseOperation.DELETE;
      case DELETE_ALL:
        return DatabaseOperation.DELETE_ALL;
      case INSERT:
        return DatabaseOperation.INSERT;
      case NONE:
        return DatabaseOperation.NONE;
      case REFRESH:
        return DatabaseOperation.REFRESH;
      case TRUNCATE_TABLE:
        return DatabaseOperation.TRUNCATE_TABLE;
      case UPDATE:
        return DatabaseOperation.UPDATE;
      default:
        throw new IllegalArgumentException("Invalid DatasetOperationType [" + type + "]");
    }

  }

}
