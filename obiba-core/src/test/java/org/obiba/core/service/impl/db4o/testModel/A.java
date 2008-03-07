package org.obiba.core.service.impl.db4o.testModel;

import java.util.List;

public class A {

  private Integer id;

  private Integer value;

  private List<B> bees;

  public final Integer getId() {
    return id;
  }

  public final void setId(Integer id) {
    this.id = id;
  }

  public final Integer getValue() {
    return value;
  }

  public final void setValue(Integer value) {
    this.value = value;
  }

  public final List<B> getBees() {
    return bees;
  }

  public final void setBees(List<B> bees) {
    this.bees = bees;
  }

}
