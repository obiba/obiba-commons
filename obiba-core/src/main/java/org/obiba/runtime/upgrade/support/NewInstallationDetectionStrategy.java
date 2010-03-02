package org.obiba.runtime.upgrade.support;

import org.obiba.runtime.upgrade.VersionProvider;

public interface NewInstallationDetectionStrategy {

  public boolean isNewInstallation(VersionProvider runtimeVersionProvider);

}
