package org.obiba.runtime.upgrade;

import org.obiba.runtime.Version;

public interface VersionModifier extends VersionProvider {

  void setVersion(Version version);

}
