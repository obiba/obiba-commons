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

import java.io.IOException;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.obiba.git.GitUtils;

/**
 * Base class for all  GIT commands. All subclasses are immutable and must be created by their respective builders
 *
 * @param <T> type of builder
 */
public abstract class AbstractCommand<T> implements Command<T> {

  final Repository repository;

  AbstractCommand(@NotNull Repository repository) {
    this.repository = repository;
  }

  @Nullable
  RevCommit getCommitById(String commitId) throws IOException {
    ObjectId id = repository.resolve(commitId);
    return id == null ? null : new RevWalk(repository).parseCommit(id);
  }

  boolean isHead(String commitId) throws IOException {
    return GitUtils.HEAD_COMMIT_ID.equals(commitId) || getHeadCommitId().equals(commitId);
  }

  ObjectId getHeadCommit() throws IOException {
    return repository.resolve(GitUtils.HEAD_COMMIT_ID);
  }

  String getHeadCommitId() throws IOException {
    ObjectId id = getHeadCommit();
    return id == null ? "" : id.getName();
  }

  /**
   * Base class for all command builder.
   *
   * @param <T> subclass type
   */
  @SuppressWarnings("ParameterHidesMemberVariable")
  protected static class Builder<T extends Builder<?>> {

    final Repository repository;

    String path;

    @SuppressWarnings("ConstantConditions")
    Builder(@NotNull Repository repository) {
      this.repository = repository;
    }

    @SuppressWarnings("unchecked")
    public T addPath(@Nullable String path) {
      this.path = path;
      return (T) this;
    }

  }

}
