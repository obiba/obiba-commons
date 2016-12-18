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
import java.io.InputStream;
import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.eclipse.jgit.api.Git;

import com.google.common.collect.ImmutableSet;

public class ReadFilesCommand extends AbstractGitCommand<Set<InputStream>> {

  private String commitId;

  private String filter;

  private boolean recursive = false;

  private ReadFilesCommand(File repositoryPath, @Nullable File workPath) {
    super(repositoryPath, workPath);
  }

  @Override
  public Set<InputStream> execute(Git git) {
    ListFilesCommand listFilesCommand = new ListFilesCommand.Builder(getRepositoryPath()).recursive(recursive)
        .filter(filter).commitId(commitId).build();

    Set<String> files = listFilesCommand.execute(git);
    ImmutableSet.Builder<InputStream> fileContents = ImmutableSet.builder();
    for(String file : files) {
      fileContents.add(new ReadFileCommand.Builder(getRepositoryPath(), file).build().execute(git));
    }

    return fileContents.build();
  }

  @SuppressWarnings("ParameterHidesMemberVariable")
  public static class Builder {

    private final ReadFilesCommand command;

    public Builder(@NotNull File repositoryPath) {
      this(repositoryPath, null);
    }

    public Builder(@NotNull File repositoryPath, @Nullable File workPath) {
      command = new ReadFilesCommand(repositoryPath, workPath);
    }

    public Builder commitId(String commitId) {
      command.commitId = commitId;
      return this;
    }

    public Builder filter(String filter) {
      command.filter = filter;
      return this;
    }

    public Builder recursive(boolean recursive) {
      command.recursive = recursive;
      return this;
    }

    public ReadFilesCommand build() {
      return command;
    }
  }

}
