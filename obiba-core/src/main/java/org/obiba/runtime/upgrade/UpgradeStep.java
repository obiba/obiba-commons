package org.obiba.runtime.upgrade;

import org.obiba.runtime.Version;

public interface UpgradeStep {

  public String getDescription();

  public Version getAppliesTo();

  public void execute(Version currentVersion);

}
