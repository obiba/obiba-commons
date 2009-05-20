package org.obiba.runtime.upgrade;

import org.obiba.runtime.Version;

public interface UpgradeManager {

  public Version getCurrentVersion();

  public Version getRuntimeVersion();

  public boolean requiresUpgrade();

  public void executeUpgrade() throws UpgradeException;

}
