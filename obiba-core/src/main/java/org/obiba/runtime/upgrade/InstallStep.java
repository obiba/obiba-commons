/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.runtime.upgrade;

import org.obiba.runtime.Version;

/**
 * Interface for an installation step.
 * <p>
 * <code>InstallStep</code>s are executed by the <code>UpgradeManager</code> whenever it detects
 * a new installation.
 * </p>
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
