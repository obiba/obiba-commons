/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.git;

public class GitException extends RuntimeException {

  private static final long serialVersionUID = -8432113180151022358L;

  public GitException(String message) {
    super(message);
  }

  public GitException(String message, Throwable cause) {
    super(message, cause);
  }

  public GitException(Throwable cause) {
    super(cause);
  }

  public GitException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
