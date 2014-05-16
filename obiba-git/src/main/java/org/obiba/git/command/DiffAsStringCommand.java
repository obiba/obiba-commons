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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.obiba.git.GitException;

/**
 * Opal GIT command used to extract the diff between two commits. By default, the diff is between the given commit and
 * its parent. By providing a valid 'nthCommit' value, the command will extract the appropriate diff from the repo.
 */
public class DiffAsStringCommand extends AbstractGitCommand<Iterable<String>> {

  private String path;

  private final String commitId;

  private String previousCommitId;

  private int nthCommit = 1;

  protected DiffAsStringCommand(@NotNull File repositoryPath, @Nullable File workPath, String commitId) {
    super(repositoryPath, workPath);
    this.commitId = commitId;
  }

  @Override
  public Iterable<String> execute(Git git) {

    try {
      try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
        DiffFormatter formatter = new DiffFormatter(out);
        formatter.setRepository(git.getRepository());
        formatter.setDiffComparator(RawTextComparator.DEFAULT);
        formatter.setDetectRenames(true);

        DiffCommand diffCommand = new DiffCommand.Builder(getRepositoryPath(), commitId).path(path)
            .previousCommitId(previousCommitId).nthCommit(nthCommit).build();
        Collection<String> diffEntries = new ArrayList<>();
        for(DiffEntry diffEntry : diffCommand.execute(git)) {
          formatter.format(diffEntry);
          diffEntry.getOldId();
          diffEntries.add(out.toString("UTF-8"));
          out.reset();
        }
        return diffEntries;
      }

    } catch(IOException e) {
      throw new GitException(e);
    }
  }

  @SuppressWarnings("ParameterHidesMemberVariable")
  public static class Builder {

    private final DiffAsStringCommand command;

    public Builder(@NotNull File repositoryPath, String commitId) {
      this(repositoryPath, null, commitId);
    }

    public Builder(@NotNull File repositoryPath, @Nullable File workPath, String commitId) {
      command = new DiffAsStringCommand(repositoryPath, workPath, commitId);
    }

    public Builder path(String path) {
      command.path = path;
      return this;
    }

    public Builder previousCommitId(String previousCommitId) {
      command.previousCommitId = previousCommitId;
      return this;
    }

    public Builder nthCommit(int nthCommit) {
      command.nthCommit = nthCommit;
      return this;
    }

    public DiffAsStringCommand build() {
      return command;
    }
  }

}
