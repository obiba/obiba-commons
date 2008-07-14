package org.obiba.core.service.impl.db4o.testModel;


public class C {

  private Integer id;

  private Integer value;

  private B parent;

  public C(Integer value, B parent) {
    this.value = value;
    this.parent = parent;
  }
  
  public C() {
    
  }

  public final B getParent() {
    return parent;
  }

  public final void setParent(B parent) {
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
