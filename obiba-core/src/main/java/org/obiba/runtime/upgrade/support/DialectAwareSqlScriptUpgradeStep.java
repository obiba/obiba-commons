package org.obiba.runtime.upgrade.support;

import java.io.IOException;

import org.obiba.runtime.Version;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

public class DialectAwareSqlScriptUpgradeStep extends SqlScriptUpgradeStep {
  //
  // Instance Variables
  //

  private String scriptDialect;

  private String scriptBasename;

  private Resource scriptLocation;

  private DialectMapper dialectMapper;

  //
  // SqlScriptUpgradeStep Methods
  //

  @Override
  public Resource getScript() {
    Resource scriptToExecute = null;

    scriptToExecute = super.getScript();

    // If no specific script has been set, use the dialect-specific script.
    if(scriptToExecute == null) {
      scriptToExecute = getDialectSpecificScript();
    }

    // If no dialect-specific script is available, fall back to a common script.
    if(scriptToExecute == null) {
      scriptToExecute = getCommonScript();
    }

    return scriptToExecute;
  }

  @Override
  public void execute(Version currentVersion, SimpleJdbcTemplate template) {
    if(getScript() == null) {
      throw new RuntimeException("No script to execute (either no script has been set, or none is compatible with the current database/dialect)");
    }

    super.execute(currentVersion, template);
  }

  //
  // Methods
  //

  public String getScriptDialect() {
    return scriptDialect;
  }

  public void setScriptDialect(String scriptDialect) {
    this.scriptDialect = scriptDialect;
  }

  public String getScriptBasename() {
    return scriptBasename;
  }

  public void setScriptBasename(String scriptBasename) {
    this.scriptBasename = scriptBasename;
  }

  public Resource getScriptLocation() {
    return scriptLocation;
  }

  public void setScriptLocation(Resource scriptLocation) {
    this.scriptLocation = scriptLocation;
  }

  public DialectMapper getDialectMapper() {
    return dialectMapper;
  }

  public void setDialectMapper(DialectMapper dialectMapper) {
    this.dialectMapper = dialectMapper;
  }

  protected Resource getDialectSpecificScript() {
    Resource dialectSpecificScript = null;

    if(scriptDialect != null && scriptBasename != null && scriptLocation != null) {
      try {
        dialectSpecificScript = scriptLocation.createRelative(scriptBasename + "-" + getScriptDialectShortName() + ".sql");
      } catch(IOException ex) {
        ex.printStackTrace();
      }
    }

    return dialectSpecificScript;
  }

  protected Resource getCommonScript() {
    Resource commonScript = null;

    if(scriptBasename != null && scriptLocation != null) {
      try {
        commonScript = scriptLocation.createRelative(scriptBasename + ".sql");
      } catch(IOException ex) {
        ex.printStackTrace();
      }
    }

    return commonScript;
  }

  /**
   * Returns the "short" version of the dialect name.
   */
  protected String getScriptDialectShortName() {
    if(scriptDialect != null) {
      // The "long" name looks like this, for example: org.hibernate.dialect.MySQL5InnoDBDialect
      // The "short" name (assuming no dialectMapper) looks like this: MySQL5InnoDB
      int lastPeriodIndex = scriptDialect.lastIndexOf('.');
      int dialectSuffix = scriptDialect.lastIndexOf("Dialect");

      String shortName = scriptDialect.substring(lastPeriodIndex + 1, dialectSuffix);
      if(dialectMapper != null) {
        shortName = dialectMapper.mapDialect(shortName);
      }

      return shortName;
    }

    return null;
  }
}
