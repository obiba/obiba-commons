package org.obiba.runtime.upgrade.support;

import org.obiba.runtime.Version;
import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;

public class VersionPropertyEditorRegistrar implements PropertyEditorRegistrar {
  @Override
  public void registerCustomEditors(PropertyEditorRegistry registry) {
    registry.registerCustomEditor(Version.class, new VersionPropertyEditor());
  }
}
