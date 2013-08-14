package org.obiba.runtime.upgrade.support;

import org.obiba.runtime.upgrade.InstallStep;
import org.obiba.runtime.upgrade.UpgradeStep;

public interface UpgradeManagerListener {

  void onBeforeStep(InstallStep step);

  void onAfterStep(InstallStep step);

  void onFailedStep(InstallStep step, Exception e);

  void onBeforeStep(UpgradeStep step);

  void onAfterStep(UpgradeStep step);

  void onFailedStep(UpgradeStep step, Exception e);
}
