package org.obiba.core.test.spring;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Datasets {

  public Dataset[] value() default { };

}
