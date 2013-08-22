package org.obiba.runtime.upgrade;

import org.obiba.runtime.Version;

/**
 * Interface for an installation step.
 * <p/>
 * <code>InstallStep</code>s are executed by the <code>UpgradeManager</code> whenever it detects
 * a new installation.
 *
 * @author cag-dspathis
 */
public interface InstallStep {

  /**
   * Returns a description of the <code>InstallStep</code>.
   *
   * @return description
   */
  String getDescription();

  /**
   * Executes the <code>InstallStep</code>.
   *
   * @param currentVersion
   */
  void execute(Version currentVersion);

}
