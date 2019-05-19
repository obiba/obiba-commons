/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.core.test.spring;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
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
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.util.ReflectionUtils;

/**
 * TestExecutionListener implementation that handles the {@link Dataset} annotation.
 * <p>
 * Using this listener (through {@link TestExecutionListeners} allows seeding a test
 * database before executing unit test methods.
 * </p>
 */
public class DbUnitAwareTestExecutionListener extends AbstractTestExecutionListener {

  private static final Logger log = LoggerFactory.getLogger(DbUnitAwareTestExecutionListener.class);

  @Override
  public void afterTestMethod(TestContext context) throws Exception {
    log.debug("{}.afterTestMethod() for context {}", getClass().getSimpleName(), context);

    DbUnitTestContextAdapter adapter = new DbUnitTestContextAdapter(context);
    handleElement(adapter, adapter.getTestMethod(), false);
  }

  @Override
  public void beforeTestMethod(TestContext context) throws Exception {
    log.debug("{}.beforeTestMethod() for context {}", getClass().getSimpleName(), context);
    DbUnitTestContextAdapter adapter = new DbUnitTestContextAdapter(context);
    handleElement(adapter, adapter.getTestMethod(), true);
  }

  @Override
  public void prepareTestInstance(TestContext context) throws Exception {
    log.debug("{}.prepareTestInstance() for context {}", getClass().getSimpleName(), context);
    DbUnitTestContextAdapter adapter = new DbUnitTestContextAdapter(context);

    if(adapter.getAttribute("dbUnit" + context.getTestClass()) == null) {
      handleElement(adapter, adapter.getTestClass(), true);
      adapter.setAttribute("dbUnit" + adapter.getTestClass(), new Object());
    }
  }

  private void handleElement(DbUnitTestContextAdapter contextAdapter, AnnotatedElement element, boolean before)
      throws Exception {
    Datasets ds = element.getAnnotation(Datasets.class);
    if(ds != null) {
      for(Dataset dataset : ds.value()) {
        handleAnnotation(contextAdapter, dataset, before);
      }
    } else {
      Dataset da = element.getAnnotation(Dataset.class);
      if(da != null) {
        handleAnnotation(contextAdapter, da, before);
      } else {
        log.debug("No {} annotation found on element {}.", Dataset.class.getSimpleName(), element);
      }
    }
  }

  private void handleAnnotation(DbUnitTestContextAdapter contextAdapter, Dataset datasetAnnotation, boolean before)
      throws Exception {
    log.debug("Handling annotation {}", datasetAnnotation);

    String className = contextAdapter.getTestClass().getSimpleName();
    String dataSourceBeanName = datasetAnnotation.dataSourceBean();

    DataSource dataSource = (DataSource) contextAdapter.getApplicationContext().getBean(dataSourceBeanName);
    IDatabaseConnection connection = new DatabaseDataSourceConnection(dataSource);
    connection.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new HsqldbDataTypeFactory());
    try {
      String filenames[] = datasetAnnotation.filenames();
      if(filenames == null || filenames.length == 0) {
        filenames = new String[] { className + ".xml" };
      }
      for(String filename : filenames) {
        seedDatabase(contextAdapter, datasetAnnotation, before, className, connection, filename);
      }
    } finally {
      try {
        connection.close();
      } catch(SQLException e) {
        // Ignore so we don't hide the pertinent exception if any...
      }
    }
  }

  private void seedDatabase(DbUnitTestContextAdapter contextAdapter, Dataset datasetAnnotation, boolean before,
      String className, IDatabaseConnection connection, String filename)
      throws IOException, SQLException, DatabaseUnitException {
    log.debug("Seeding database with dataset {}.", filename);
    InputStream is = contextAdapter.getTestClass().getResourceAsStream(filename);
    if(is == null) {
      log.error("Test case {}: cannot find resource {}.", className, filename);
    } else {
      IDataSet dataset = new FlatXmlDataSet(is);
      try {
        getDbUnitOp(before ? datasetAnnotation.beforeOperation() : datasetAnnotation.afterOperation())
            .execute(connection, dataset);
      } catch(DatabaseUnitException e) {
        log.error("Exception while inserting dataset filename {} for test case {}", filename, className,
            e.getMessage());
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

  /**
   * Adapter class to convert Spring's {@link TestContext} to a {@link DbUnitTestContext}. Since Spring 4.0 change the
   * TestContext class from a class to an interface this method uses reflection.
   */
  private static class DbUnitTestContextAdapter {

    private static final Method GET_TEST_CLASS;

    private static final Method GET_TEST_METHOD;

    private static final Method GET_TEST_EXCEPTION;

    private static final Method GET_APPLICATION_CONTEXT;

    private static final Method GET_ATTRIBUTE;

    private static final Method SET_ATTRIBUTE;

    static {
      try {
        GET_TEST_CLASS = TestContext.class.getMethod("getTestClass");
        GET_TEST_METHOD = TestContext.class.getMethod("getTestMethod");
        GET_TEST_EXCEPTION = TestContext.class.getMethod("getTestException");
        GET_APPLICATION_CONTEXT = TestContext.class.getMethod("getApplicationContext");
        GET_ATTRIBUTE = TestContext.class.getMethod("getAttribute", String.class);
        SET_ATTRIBUTE = TestContext.class.getMethod("setAttribute", String.class, Object.class);
      } catch(Exception ex) {
        throw new IllegalStateException(ex);
      }
    }

    private final TestContext testContext;

    private DbUnitTestContextAdapter(TestContext testContext) {
      this.testContext = testContext;
    }

    public Class<?> getTestClass() {
      return (Class<?>) ReflectionUtils.invokeMethod(GET_TEST_CLASS, testContext);
    }

    public Method getTestMethod() {
      return (Method) ReflectionUtils.invokeMethod(GET_TEST_METHOD, testContext);
    }

    public Throwable getTestException() {
      return (Throwable) ReflectionUtils.invokeMethod(GET_TEST_EXCEPTION, testContext);
    }

    public ApplicationContext getApplicationContext() {
      return (ApplicationContext) ReflectionUtils.invokeMethod(GET_APPLICATION_CONTEXT, testContext);
    }

    public Object getAttribute(String name) {
      return ReflectionUtils.invokeMethod(GET_ATTRIBUTE, testContext, name);
    }

    public void setAttribute(String name, Object value) {
      ReflectionUtils.invokeMethod(SET_ATTRIBUTE, testContext, name, value);
    }

  }

}
