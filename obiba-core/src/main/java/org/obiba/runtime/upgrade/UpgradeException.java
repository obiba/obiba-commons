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

public class UpgradeException extends Exception {

  private static final long serialVersionUID = -5972984899869970156L;

  public UpgradeException(Exception cause) {
    super(cause);
  }
}
