package org.obiba.runtime.upgrade;

import org.obiba.runtime.Version;

public interface UpgradeStep {

  String getDescription();

  Version getAppliesTo();

  void execute(Version currentVersion);

}
