package org.obiba.runtime.upgrade.support;

public interface DialectMapper {

  /**
   * Given a dialect name, maps it to another name.
   * 
   * @param dialect dialect name
   * @return mapped dialect name
   */
  public String mapDialect(String dialect);
}
