package org.obiba.runtime.upgrade.support;

import org.obiba.runtime.upgrade.UpgradeStep;

public interface UpgradeStepExecutionListener {
  
  public void onBeforeStep(UpgradeStep step);

  public void onAfterStep(UpgradeStep step);

  public void onFailedStep(UpgradeStep step, Exception e);

}
