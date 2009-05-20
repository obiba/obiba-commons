package org.obiba.runtime.upgrade.support;

import org.obiba.runtime.upgrade.UpgradeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingUpgradeStepExecutionListener extends AbstractUpgradeStepExecutionListener {

  private static final Logger log = LoggerFactory.getLogger(LoggingUpgradeStepExecutionListener.class);

  @Override
  public void onBeforeStep(UpgradeStep step) {
    super.onBeforeStep(step);
    log.info("About to execute VersionMigrationStep. Description: '{}'", step.getDescription());
    log.info("VersionMigrationStep applies to version '{}'.", step.getAppliesTo());
    log.info("VersionMigrationStep type: {}", step.getClass().getName());
  }

  @Override
  public void onAfterStep(UpgradeStep step) {
    super.onAfterStep(step);
    log.info("Successfully executed VersionMigrationStep. Description: '{}'", step.getDescription());
  }

  @Override
  public void onFailedStep(UpgradeStep step, Exception e) {
    super.onFailedStep(step, e);
    log.error("Error during VersionMigrationStep execution. Error message: '{}'", e.getMessage());
    log.error("VersionMigrationStep applies to version '{}'.", step.getAppliesTo());
    log.error("VersionMigrationStep description: '{}'.", step.getDescription());
    log.error("Exception:", e);
  }
}
