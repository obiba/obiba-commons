package org.obiba.core.domain;

import java.io.Serializable;

/**
 * Implemented by persistent entities.
 */
public interface IEntity extends Serializable {

  Serializable getId();

  void setId(Serializable id);

}
