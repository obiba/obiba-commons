package org.obiba.runtime.upgrade.support;

public class DefaultDialectMapper implements DialectMapper {
  //
  // DialectMapper Methods
  //

  public String mapDialect(String dialect) {
    if(dialect.equals("MySQL5InnoDB")) {
      return "MySQLInnoDB";
    }

    return dialect;
  }

}
