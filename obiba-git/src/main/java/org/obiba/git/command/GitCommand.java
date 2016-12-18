/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.git.command;

import java.io.File;

import org.eclipse.jgit.api.Git;

public interface GitCommand<T> {

  /**
   * Execute the command.
   * @param git The cloned repository object.
   * @return
   */
  T execute(Git git);

  /**
   * Bare repository path. Folder name is expected to end with ".git".
   * @return
   */
  File getRepositoryPath();

  /**
   * Work directory path where the bare repository will be cloned if no clone is found.
   * @return
   */
  File getWorkPath();

  /**
   * Delete the cloned repository in the work directory after command execution.
   * @return
   */
  boolean deleteClone();
}

