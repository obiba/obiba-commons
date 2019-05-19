/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.git.command;

import java.io.File;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.google.common.io.Files;

/**
 * Base class for all  GIT commands. All subclasses are immutable and must be created by their respective builders
 *
 * @param <T> type of builder
 */
public abstract class AbstractGitCommand<T> implements GitCommand<T> {

  @NotNull
  private final File repositoryPath;

  @Nullable
  private final File workPath;

  protected AbstractGitCommand(@NotNull File repositoryPath) {
    this(repositoryPath, null);
  }

  protected AbstractGitCommand(@NotNull File repositoryPath, @Nullable File workPath) {
    this.repositoryPath = repositoryPath;
    this.workPath = workPath;
    if (workPath != null && !workPath.exists()) workPath.mkdirs();
  }

  @Override
  public File getRepositoryPath() {
    return repositoryPath;
  }

  @Override
  public File getWorkPath() {
    return workPath == null ? Files.createTempDir() : workPath;
  }

  @Override
  public boolean deleteClone() {
    return workPath == null;
  }
}
