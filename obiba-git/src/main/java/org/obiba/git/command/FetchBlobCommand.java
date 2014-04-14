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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import javax.validation.constraints.NotNull;

import org.eclipse.jgit.api.Git;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.CharStreams;

/**
 * Opal GIT command used to extract the content of a file. Folders are not supported.
 */
public class FetchBlobCommand extends AbstractGitCommand<String> {

  private final String path;

  private String commitId;

  private String encoding;

  private FetchBlobCommand(@NotNull File repositoryPath, @NotNull String path) {
    super(repositoryPath);
    this.path = path;
  }

  @Override
  public String execute(Git git) throws Exception {
    ReadFileCommand readFileCommand = new ReadFileCommand.Builder(getRepositoryPath(), path).commitId(commitId).build();
    InputStream inputStream = readFileCommand.execute(git);
    return CharStreams.toString(new InputStreamReader(inputStream, getEncoding()));
  }

  private Charset getEncoding() {
    return Strings.isNullOrEmpty(encoding) ? Charsets.UTF_8 : Charset.forName(encoding);
  }

  @SuppressWarnings("ParameterHidesMemberVariable")
  public static class Builder {
    private final FetchBlobCommand command;

    public Builder(@NotNull File repositoryPath, @NotNull String path) {
      command = new FetchBlobCommand(repositoryPath, path);
    }

    public Builder commitId(String commitId) {
      command.commitId = commitId;
      return this;
    }

    public Builder encoding(String encoding) {
      command.encoding = encoding;
      return this;
    }

    public FetchBlobCommand build() {
      return command;
    }
  }
}