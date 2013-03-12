package org.obiba.runtime.upgrade.support;

import java.beans.PropertyEditorSupport;

import org.obiba.runtime.Version;

public class VersionPropertyEditor extends PropertyEditorSupport {
  //
  // PropertyEditorSupport Methods
  //

  @Override
  public void setAsText(String versionString) {
    Version version = new Version(versionString);
    setValue(version);
  }
}
