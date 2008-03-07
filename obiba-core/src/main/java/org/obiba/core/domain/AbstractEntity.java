package org.obiba.core.domain;

import java.io.Serializable;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

@MappedSuperclass
@TypeDef(name="long", typeClass=Long.class)
abstract public class AbstractEntity implements IEntity {

  @Id
  @GeneratedValue(strategy=GenerationType.AUTO)
  @Type(type="long")
  private Serializable id;
  
  public Serializable getId() {
    return id;
  }

  public void setId(Serializable id) {
    this.id = id;
  }

}
