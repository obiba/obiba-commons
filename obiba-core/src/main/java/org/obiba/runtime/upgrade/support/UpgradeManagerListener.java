package org.obiba.runtime.upgrade.support;

import org.obiba.runtime.upgrade.InstallStep;
import org.obiba.runtime.upgrade.UpgradeStep;

public interface UpgradeManagerListener {

  public void onBeforeStep(InstallStep step);

  public void onAfterStep(InstallStep step);

  public void onFailedStep(InstallStep step, Exception e);

  public void onBeforeStep(UpgradeStep step);

  public void onAfterStep(UpgradeStep step);

  public void onFailedStep(UpgradeStep step, Exception e);
}
