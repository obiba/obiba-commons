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
