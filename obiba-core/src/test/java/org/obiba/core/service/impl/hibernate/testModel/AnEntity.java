package org.obiba.core.service.impl.hibernate.testModel;

import javax.persistence.Entity;

import org.obiba.core.domain.AbstractEntity;

@Entity
public class AnEntity extends AbstractEntity {

  private static final long serialVersionUID = -9167410848313046815L;

  private String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
