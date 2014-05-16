package org.obiba.git.command;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.obiba.git.GitException;
import org.obiba.git.GitUtils;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

public class ListFilesCommand extends AbstractGitCommand<Set<String>> {

  private String filter;

  private String commitId;

  private boolean recursive = false;

  private ListFilesCommand(File repositoryPath, @Nullable File workPath) {
    super(repositoryPath, workPath);
  }

  @Override
  public Set<String> execute(Git git) {
    Repository repository = git.getRepository();
    RevWalk walk = new RevWalk(repository);
    try {
      RevCommit commit = walk.parseCommit(
          ObjectId.fromString(Strings.isNullOrEmpty(commitId) ? GitUtils.getHeadCommitId(repository) : commitId));

      TreeWalk commitWalk = new TreeWalk(repository);
      commitWalk.addTree(commit.getTree());
      commitWalk.setRecursive(recursive);

      ImmutableSet.Builder<String> files = ImmutableSet.builder();
      if(Strings.isNullOrEmpty(filter)) {
        while(commitWalk.next()) {
          files.add(commitWalk.getPathString());
        }
      } else {
        Pattern pattern = Pattern.compile(filter);
        while(commitWalk.next()) {
          String filePath = commitWalk.getPathString();
          Matcher matcher = pattern.matcher(filePath);
          if(matcher.find()) files.add(filePath);
        }
      }

      return files.build();

    } catch(IOException e) {
      throw new GitException(e);
    }

  }

  @SuppressWarnings("ParameterHidesMemberVariable")
  public static class Builder {

    private final ListFilesCommand command;

    public Builder(@NotNull File repositoryPath) {
      this(repositoryPath, null);
    }

    public Builder(@NotNull File repositoryPath, @Nullable File workPath) {
      command = new ListFilesCommand(repositoryPath, workPath);
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

    public ListFilesCommand build() {
      return command;
    }
  }

}
