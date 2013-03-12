package org.obiba.runtime.jdbc;

import org.springframework.util.PatternMatchUtils;

public class DatabaseProduct {

  public String[] databaseProductNames;

  public String normalizedName;

  public String getNormalizedName() {
    return normalizedName;
  }

  public boolean isForProductName(String dbProductName) {
    return PatternMatchUtils.simpleMatch(databaseProductNames, dbProductName);
  }

  @Override
  public String toString() {
    return getNormalizedName();
  }

}
