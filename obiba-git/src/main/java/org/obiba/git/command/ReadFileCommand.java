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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import jakarta.validation.constraints.NotNull;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.obiba.git.GitException;
import org.obiba.git.NoSuchGitRepositoryException;
import org.springframework.lang.Nullable;

import static com.google.common.base.Strings.isNullOrEmpty;

public class ReadFileCommand extends AbstractGitCommand<InputStream> {

  private final String path;

  private String commitId;

  private String tag;

  private ReadFileCommand(@NotNull File repositoryPath, @Nullable File workPath, String path) {
    super(repositoryPath, workPath);
    this.path = path;
  }

  @Override
  public InputStream execute(Git git) {
    try {
      Repository repository = git.getRepository();
      if(!isNullOrEmpty(tag)) {
        return readTag(repository);
      }
      if(!isNullOrEmpty(commitId)) {
        return readCommit(repository);
      }
      // read current revision
      return new FileInputStream(new File(repository.getWorkTree(), path));
    } catch(IOException e) {
      throw new GitException(e);
    }
  }

  private InputStream readTag(Repository repository) throws IOException {
    Ref ref = repository.getTags().get(tag);
    ObjectId objectId = repository.resolve(ref.getObjectId().getName());
    if(objectId == null) {
      throw new GitException(String.format("No commit with id '%s' for tag '%s'", ref.getObjectId(), tag));
    }
    return read(repository, objectId);
  }

  private InputStream readCommit(Repository repository) throws IOException {
    ObjectId objectId = repository.resolve(commitId);
    if(objectId == null) {
      throw new GitException(String.format("No commit with id '%s'", commitId));
    }
    return read(repository, objectId);
  }

  private InputStream read(Repository repository, @NotNull ObjectId objectId) throws IOException {
    ObjectReader reader = repository.newObjectReader();
    RevTree tree = new RevWalk(reader).parseCommit(objectId).getTree();
    TreeWalk treeWalk = TreeWalk.forPath(reader, path, tree);
    if(treeWalk == null) {
      throw new GitException(String.format("Path '%s' was not found in commit '%s'", path, objectId));
    }
    return new ByteArrayInputStream(reader.open(treeWalk.getObjectId(0)).getBytes());
  }

  @SuppressWarnings("ParameterHidesMemberVariable")
  public static class Builder {

    private final ReadFileCommand command;

    public Builder(@NotNull File repositoryPath, String path) {
      this(repositoryPath, null, path);
    }

    public Builder(@NotNull File repositoryPath, @Nullable File workPath, String path) {
      if(!repositoryPath.exists() || !repositoryPath.isDirectory()) {
        throw new NoSuchGitRepositoryException(path);
      }
      command = new ReadFileCommand(repositoryPath, workPath, path);
    }

    public Builder commitId(String commitId) {
      command.commitId = commitId;
      return this;
    }

    public Builder tag(String tag) {
      command.tag = tag;
      return this;
    }

    public ReadFileCommand build() {
      if(!isNullOrEmpty(command.tag) && !isNullOrEmpty(command.commitId)) {
        throw new IllegalArgumentException("Choose between tag or commitId but don't specify both");
      }
      return command;
    }
  }
}
