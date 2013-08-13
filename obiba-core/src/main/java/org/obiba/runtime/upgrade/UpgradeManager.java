package org.obiba.runtime.upgrade;

import org.obiba.runtime.Version;

public interface UpgradeManager {

  Version getCurrentVersion();

  Version getRuntimeVersion();

  boolean requiresUpgrade();

  void executeUpgrade() throws UpgradeException;

}
