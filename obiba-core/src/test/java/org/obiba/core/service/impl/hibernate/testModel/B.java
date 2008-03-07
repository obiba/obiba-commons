package org.obiba.core.service.impl.hibernate.testModel;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class B {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Integer id;

  private Integer value;

  @ManyToOne
  private A parent;

  @OneToMany(mappedBy = "parent")
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
