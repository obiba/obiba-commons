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
import java.io.IOException;
import java.nio.charset.Charset;

import javax.validation.constraints.NotNull;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.obiba.git.GitException;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;

/**
 * Opal GIT command used to extract the content of a file. Folders are not supported.
 */
public class FetchBlobCommand extends AbstractGitCommand<String> {

  private final String path;

  private final String commitId;

  private String encoding;

  private FetchBlobCommand(@NotNull File repositoryPath, @NotNull String path, @NotNull String commitId) {
    super(repositoryPath);
    this.path = path;
    this.commitId = commitId;
  }

  @Override
  public String execute(Git git) {
    try {
      Repository repository = git.getRepository();
      ObjectReader reader = repository.newObjectReader();
      TreeWalk treewalk = getPathTreeWalk(repository, reader);
      if(treewalk != null) {
        return new String(reader.open(treewalk.getObjectId(0)).getBytes(),
            Strings.isNullOrEmpty(encoding) ? Charsets.UTF_8 : Charset.forName(encoding));
      }
    } catch(IOException e) {
      throw new GitException(e);
    }
    throw new GitException(String.format("Path '%s' was not found in commit '%s'", path, commitId));
  }

  private TreeWalk getPathTreeWalk(Repository repository, ObjectReader reader) throws IOException {
    ObjectId id = repository.resolve(commitId);
    if(id == null) {
      throw new GitException(String.format("No commit with id '%s'", commitId));
    }

    RevWalk walk = new RevWalk(reader);
    RevCommit commit = walk.parseCommit(id);
    RevTree tree = commit.getTree();
    return TreeWalk.forPath(reader, path, tree);
  }

  @SuppressWarnings("ParameterHidesMemberVariable")
  public static class Builder {
    private final FetchBlobCommand command;

    public Builder(@NotNull File repositoryPath, @NotNull String path, @NotNull String commitId) {
      command = new FetchBlobCommand(repositoryPath, path, commitId);
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