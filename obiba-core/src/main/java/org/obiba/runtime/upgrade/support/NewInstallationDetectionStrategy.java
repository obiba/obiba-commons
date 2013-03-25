package org.obiba.runtime.upgrade.support;

import org.obiba.runtime.upgrade.VersionProvider;

public interface NewInstallationDetectionStrategy {

  boolean isNewInstallation(VersionProvider runtimeVersionProvider);

}
