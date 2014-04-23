/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.git.command;

import java.io.File;

import javax.validation.constraints.NotNull;

/**
 * Base class for all  GIT commands. All subclasses are immutable and must be created by their respective builders
 *
 * @param <T> type of builder
 */
public abstract class AbstractGitCommand<T> implements GitCommand<T> {

  @NotNull
  private final File repositoryPath;

  protected AbstractGitCommand(@NotNull File repositoryPath) {
    this.repositoryPath = repositoryPath;
  }

  @Override
  public File getRepositoryPath() {
    return repositoryPath;
  }

}
