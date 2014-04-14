package org.obiba.git.command;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.validation.constraints.NotNull;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.obiba.git.GitException;
import org.obiba.git.NoSuchGitRepositoryException;

import com.google.common.base.Strings;

public class ReadFileCommand extends AbstractGitCommand<InputStream> {

  private final String path;

  private String commitId;

  private ReadFileCommand(@NotNull File repositoryPath, String path) {
    super(repositoryPath);
    if(!repositoryPath.exists() || !repositoryPath.isDirectory()) {
      throw new NoSuchGitRepositoryException(path);
    }
    this.path = path;
  }

  @Override
  public InputStream execute(Git git) throws Exception {

    Repository repository = git.getRepository();

    if(Strings.isNullOrEmpty(commitId)) {
      return new FileInputStream(new File(repository.getWorkTree(), path));
    }

    ObjectReader reader = repository.newObjectReader();
    TreeWalk treewalk = getPathTreeWalk(repository, reader);
    if(treewalk == null) {
      throw new GitException(String.format("Path '%s' was not found in commit '%s'", path, commitId));
    }
    return new ByteArrayInputStream(reader.open(treewalk.getObjectId(0)).getBytes());
  }

  private TreeWalk getPathTreeWalk(Repository repository, ObjectReader reader) throws IOException {
    ObjectId objectId = repository.resolve(commitId);
    if(objectId == null) {
      throw new GitException(String.format("No commit with id '%s'", commitId));
    }
    RevTree tree = new RevWalk(reader).parseCommit(objectId).getTree();
    return TreeWalk.forPath(reader, path, tree);
  }

  @SuppressWarnings("ParameterHidesMemberVariable")
  public static class Builder {

    private final ReadFileCommand command;

    public Builder(@NotNull File repositoryPath, String pathInRepo) {
      command = new ReadFileCommand(repositoryPath, pathInRepo);
    }

    public Builder commitId(String commitId) {
      command.commitId = commitId;
      return this;
    }

    public ReadFileCommand build() {
      return command;
    }
  }
}
