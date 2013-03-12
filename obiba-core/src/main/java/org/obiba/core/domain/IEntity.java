package org.obiba.core.domain;

import java.io.Serializable;

/**
 * Implemented by persistent entities.
 */
public interface IEntity extends Serializable {

  public Serializable getId();

  public void setId(final Serializable id);

}
