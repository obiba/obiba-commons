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

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.PushResult;
import org.obiba.git.GitException;

@SuppressWarnings("ClassTooDeepInInheritanceTree")
public class DeleteFilesCommand extends AbstractGitWriteCommand {

  private final String filePattern;

  private DeleteFilesCommand(@NotNull File repositoryPath, @Nullable File workPath, @NotNull String pattern, String commitMessage) {
    super(repositoryPath, workPath, commitMessage);
    filePattern = pattern;
  }

  @Override
  public Iterable<PushResult> execute(Git git) {
    try {
      git.rm().addFilepattern(filePattern).call();
      return commitAndPush(git);
    } catch(GitAPIException e) {
      throw new GitException(e);
    }
  }

  public static class Builder {

    private final DeleteFilesCommand command;

    public Builder(@NotNull File repositoryPath, @NotNull String pattern, String commitMessage) {
      this(repositoryPath, null, pattern, commitMessage);
    }

    public Builder(@NotNull File repositoryPath, @Nullable File workPath, @NotNull String pattern, String commitMessage) {
      command = new DeleteFilesCommand(repositoryPath, workPath, pattern, commitMessage);
    }

    public DeleteFilesCommand build() {
      return command;
    }

  }
}
