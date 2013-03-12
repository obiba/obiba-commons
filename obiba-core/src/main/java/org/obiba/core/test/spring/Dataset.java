package org.obiba.core.test.spring;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Dataset {

  public String[] filenames() default { };

  public String dataSourceBean() default "dataSource";

  public DatasetOperationType beforeOperation() default DatasetOperationType.INSERT;

  public DatasetOperationType afterOperation() default DatasetOperationType.DELETE_ALL;

}
