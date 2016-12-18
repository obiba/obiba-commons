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
import java.io.IOException;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.obiba.git.CommitInfo;
import org.obiba.git.GitException;
import org.obiba.git.GitUtils;

/**
 * Opal GIT command used to extract the log of a repository path for a specific commit.
 */
public class CommitLogCommand extends AbstractGitCommand<CommitInfo> {

  private final String path;

  private final String commitId;

  private CommitLogCommand(@NotNull File repositoryPath, @Nullable File workPath, @NotNull String path, @NotNull String commitId) {
    super(repositoryPath, workPath);
    this.path = path;
    this.commitId = commitId;
  }

  @Override
  public CommitInfo execute(Git git) {
    Repository repository = git.getRepository();
    RevWalk walk = new RevWalk(repository);
    try {
      RevCommit commit = walk.parseCommit(ObjectId.fromString(commitId));
      if(TreeWalk.forPath(repository, path, commit.getTree()) != null) {
        // There is indeed the path in this commit
        PersonIdent personIdent = commit.getAuthorIdent();
        return new CommitInfo.Builder().authorName(personIdent.getName()) //
            .authorEmail(personIdent.getEmailAddress()) //
            .date(personIdent.getWhen()) //
            .comment(commit.getFullMessage()) //
            .commitId(commit.getName()) //
            .head(GitUtils.isHead(repository, commitId)).build();
      }
    } catch(IOException e) {
      throw new GitException(e);
    }
    throw new GitException(String.format("Path '%s' was not found in commit '%s'", path, commitId));
  }

  public static class Builder {

    private final CommitLogCommand command;

    public Builder(@NotNull File repositoryPath, @NotNull String path, @NotNull String commitId) {
      this(repositoryPath, null, path, commitId);
    }

    public Builder(@NotNull File repositoryPath, @Nullable File workPath, @NotNull String path, @NotNull String commitId) {
      command = new CommitLogCommand(repositoryPath, workPath, path, commitId);
    }

    public CommitLogCommand build() {
      return command;
    }

  }

}