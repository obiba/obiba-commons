package org.obiba.core.service.impl.db4o.testModel;

import java.util.List;

public class B {

  private Integer id;

  private Integer value;

  private A parent;

  private List<C> cees;

  public final List<C> getCees() {
    return cees;
  }

  public final void setCees(List<C> cees) {
    this.cees = cees;
  }

  public final A getParent() {
    return parent;
  }

  public final void setParent(A parent) {
    this.parent = parent;
  }

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

}
