/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
