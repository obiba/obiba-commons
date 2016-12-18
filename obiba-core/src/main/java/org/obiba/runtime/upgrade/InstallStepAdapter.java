/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
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
 * Adapts an <code>UpgradeStep</code> for use as an <code>InstallStep</code>.
 *
 * @author cag-dspathis
 */
public class InstallStepAdapter implements InstallStep {
  //
  // Instance Variables
  //

  private UpgradeStep upgradeStep;

  //
  // InstallStep Methods
  //

  @Override
  public String getDescription() {
    return upgradeStep.getDescription();
  }

  @Override
  public void execute(Version currentVersion) {
    upgradeStep.execute(currentVersion);
  }

  //
  // Methods
  //

  public void setUpgradeStep(UpgradeStep upgradeStep) {
    this.upgradeStep = upgradeStep;
  }
}