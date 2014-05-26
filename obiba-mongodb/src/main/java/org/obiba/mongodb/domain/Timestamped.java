package org.obiba.mongodb.domain;

import javax.validation.constraints.NotNull;

import org.joda.time.DateTime;

public interface Timestamped {

  @NotNull
  DateTime getCreatedDate();

  void setCreatedDate(DateTime createdDate);

  DateTime getLastModifiedDate();

  void setLastModifiedDate(DateTime lastModifiedDate);
}
