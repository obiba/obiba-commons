package org.obiba.git.command;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.validation.constraints.NotNull;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.obiba.git.CommitInfo;
import org.obiba.git.GitException;
import org.obiba.git.GitUtils;

import com.google.common.base.Strings;

public class LogsCommand extends AbstractGitCommand<Iterable<CommitInfo>> {

  private String path;

  private boolean excludeDeletedCommits = false;

  private LogsCommand(@NotNull File repositoryPath) {
    super(repositoryPath);
  }

  @Override
  public Iterable<CommitInfo> execute(Git git) {
    try {
      LogCommand logCommand = git.log();
      if(!Strings.isNullOrEmpty(path)) logCommand.addPath(path);

      Collection<CommitInfo> commits = new ArrayList<>();
      // for performance, get the id before looping thru all commits preventing resolving the id each time
      String headCommitId = GitUtils.getHeadCommitId(git.getRepository());
      // TODO find an efficient way of finding the current commit of a given path
      // One possible solution is implementing: 'git log  --ancestry-path <COMMIT_HAEH>^..HEAD'
      // For now, the list is in order of 'current .. oldest'
      boolean isCurrent = true;

      for(RevCommit commit : logCommand.call()) {
        if(excludeDeletedCommits && hasDeletedCommit(git, commit)) continue;

        String commitId = commit.getName();
        boolean isHeadCommit = headCommitId.equals(commitId);
        PersonIdent personIdent = commit.getAuthorIdent();
        commits.add(new CommitInfo.Builder().authorName(personIdent.getName()) //
            .authorEmail(personIdent.getEmailAddress()) //
            .date(personIdent.getWhen()) //
            .comment(commit.getFullMessage()) //
            .commitId(commit.getName()) //
            .current(isCurrent) //
            .head(isHeadCommit).build());
        isCurrent = false;
      }

      return commits;

    } catch(IOException | GitAPIException e) {
      throw new GitException(e);
    }
  }

  /**
   * A file commit has only one diff entry. Having many diff entries imply that the commit path corresponds to the
   * whole repository or a folder in the commit tree. In this case, we do not exclude the commit if there are modified
   * or added changes as well.
   *
   * @param git
   * @param commit
   * @return
   */
  private boolean hasDeletedCommit(Git git, RevCommit commit) {
    DiffCommand diffCommand = new DiffCommand.Builder(getRepositoryPath(), commit.getName()).path(path).build();
    for(DiffEntry diff : diffCommand.execute(git)) {
      if(DiffEntry.ChangeType.DELETE != diff.getChangeType()) {
        return false;
      }
    }
    return true;
  }

  @SuppressWarnings("ParameterHidesMemberVariable")
  public static class Builder {

    private final LogsCommand command;

    public Builder(@NotNull File repositoryPath) {
      command = new LogsCommand(repositoryPath);
    }

    public Builder path(String path) {
      command.path = path;
      return this;
    }

    public Builder excludeDeletedCommits(boolean excludeDeletedCommits) {
      command.excludeDeletedCommits = excludeDeletedCommits;
      return this;
    }

    public LogsCommand build() {
      return command;
    }
  }

}
