/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.runtime.upgrade.support;

import org.obiba.runtime.upgrade.InstallStep;
import org.obiba.runtime.upgrade.UpgradeStep;

public interface UpgradeManagerListener {

  void onBeforeStep(InstallStep step);

  void onAfterStep(InstallStep step);

  void onFailedStep(InstallStep step, Exception e);

  void onBeforeStep(UpgradeStep step);

  void onAfterStep(UpgradeStep step);

  void onFailedStep(UpgradeStep step, Exception e);
}
