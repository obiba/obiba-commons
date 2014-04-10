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

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * Opal GIT command used to extract the content of a file. Folders are not supported.
 */
public class FetchBlobCommand extends AbstractCommand<String> {

  private final String path;

  private final String commitId;

  private final String encoding;

  private FetchBlobCommand(Builder builder) {
    super(builder.repository);
    path = builder.path;
    encoding = builder.encoding;
    commitId = builder.commitId;
  }

  @Override
  public String execute() {
    try {
      ObjectReader reader = repository.newObjectReader();
      TreeWalk treewalk = getPathTreeWalk(reader);
      if(treewalk != null) {
        return new String(reader.open(treewalk.getObjectId(0)).getBytes(),
            Strings.isNullOrEmpty(encoding) ? Charsets.UTF_8 : Charset.forName(encoding));
      }
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
    throw new RuntimeException(String.format("Path '%s' was not found in commit '%s'", path, commitId));
  }

  private TreeWalk getPathTreeWalk(ObjectReader reader) throws IOException {
    ObjectId id = repository.resolve(commitId);

    if(id == null) {
      throw new RuntimeException(String.format("There are no commit for commit id '%s'", commitId));
    }

    RevWalk walk = new RevWalk(reader);
    RevCommit commit = walk.parseCommit(id);
    RevTree tree = commit.getTree();
    return TreeWalk.forPath(reader, path, tree);
  }

  /**
   * Builder class for OpalGitFetchBlobCommand
   */
  public static class Builder extends AbstractCommand.Builder<Builder> {

    private final String commitId;

    private String encoding;

    public Builder(@NotNull Repository repository, @NotNull String path, @NotNull String commitId) {
      super(repository);
      Preconditions.checkArgument(path != null, "path cannot be null.");
      Preconditions.checkArgument(commitId != null, "commitId cannot be null.");
      addPath(path);
      this.commitId = commitId;
    }

    public Builder addEncoding(String value) {
      encoding = value;
      return this;
    }

    public FetchBlobCommand build() {
      File file = new File(path);
      if(file.exists() && file.isFile()) {
        return new FetchBlobCommand(this);
      }
      throw new RuntimeException("Commit path must point to a file and not a folder");
    }

  }
}