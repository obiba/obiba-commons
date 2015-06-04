package org.obiba.core.test.spring;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Dataset {

  String[] filenames() default { };

  String dataSourceBean() default "dataSource";

  DatasetOperationType beforeOperation() default DatasetOperationType.CLEAN_INSERT;

  DatasetOperationType afterOperation() default DatasetOperationType.NONE;

}
