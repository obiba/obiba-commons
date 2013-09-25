package org.obiba.runtime.upgrade.support;

import org.obiba.runtime.upgrade.InstallStep;
import org.obiba.runtime.upgrade.UpgradeStep;

public class AbstractUpgradeManagerListener implements UpgradeManagerListener {

  @Override
  public void onAfterStep(InstallStep step) {
  }

  @Override
  public void onBeforeStep(InstallStep step) {
  }

  @Override
  public void onFailedStep(InstallStep step, Exception e) {
  }

  @Override
  public void onAfterStep(UpgradeStep step) {
  }

  @Override
  public void onBeforeStep(UpgradeStep step) {
  }

  @Override
  public void onFailedStep(UpgradeStep step, Exception e) {
  }

}
