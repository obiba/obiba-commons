package org.obiba.runtime.upgrade.support;

import org.obiba.runtime.upgrade.VersionProvider;

public class NullVersionNewInstallationDetectionStrategy implements NewInstallationDetectionStrategy {

  private VersionProvider versionProvider;

  @Override
  public boolean isNewInstallation(VersionProvider runtimeVersionProvider) {
    return versionProvider.getVersion() == null;
  }

  public void setVersionProvider(VersionProvider versionProvider) {
    this.versionProvider = versionProvider;
  }
}
