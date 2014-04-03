package org.obiba.runtime.jdbc;

import org.springframework.util.PatternMatchUtils;

@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD",
    justification = "XML deserialization")
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
