/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.runtime.upgrade.support;

import org.obiba.runtime.upgrade.InstallStep;
import org.obiba.runtime.upgrade.UpgradeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingUpgradeManagerListener extends AbstractUpgradeManagerListener {

  private static final Logger log = LoggerFactory.getLogger(LoggingUpgradeManagerListener.class);

  @Override
  public void onBeforeStep(InstallStep step) {
    super.onBeforeStep(step);
    log.info("About to execute Installation Step. Description: '{}'", step.getDescription());
    log.info("Installation Step type: {}", step.getClass().getName());
  }

  @Override
  public void onAfterStep(InstallStep step) {
    super.onAfterStep(step);
    log.info("Successfully executed Installation Step. Description: '{}'", step.getDescription());
  }

  @Override
  public void onFailedStep(InstallStep step, Exception e) {
    super.onFailedStep(step, e);
    log.error("Error during Installation Step execution. Error message: '{}'", e.getMessage());
    log.error("Installation Step description: '{}'.", step.getDescription());
    log.error("Exception:", e);
  }

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
