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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotNull;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.obiba.git.CommitInfo;
import org.obiba.git.GitException;
import org.obiba.git.GitUtils;
import org.obiba.git.TagInfo;

import com.google.common.base.Strings;
import jakarta.annotation.Nullable;

public class LogsCommand extends AbstractGitCommand<Iterable<CommitInfo>> {

  private String path;

  private boolean excludeDeletedCommits = false;

  private String tagsPrefix;

  private LogsCommand(@NotNull File repositoryPath, @Nullable File workPath) {
    super(repositoryPath, workPath);
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
      Map<String, List<String>> tagsByCommit = tagsPrefix == null ? Map.of() : getTagsByCommit(git);

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
            .tags(tagsByCommit.get(commitId)) //
            .head(isHeadCommit).build());
        isCurrent = false;
      }

      return commits;

    } catch(IOException | GitAPIException e) {
      throw new GitException(e);
    }
  }

  /**
   * Map the tags under the prefix to the commits of the log. With a path, a tag goes on the latest commit of the path
   * at the tagged commit, i.e. the version of the path at the tag time. A tag made before the path existed is left out.
   */
  private Map<String, List<String>> getTagsByCommit(Git git) throws IOException, GitAPIException {
    Map<String, List<String>> tagsByCommit = new HashMap<>();
    for(TagInfo tag : TagListCommand.listTags(git.getRepository(), tagsPrefix)) {
      String commitId = tag.getCommitId();
      if(!Strings.isNullOrEmpty(path)) {
        // ponytail: one log per tag, fine for tens of tags
        Iterator<RevCommit> commits = git.log().add(ObjectId.fromString(commitId)).addPath(path).setMaxCount(1).call()
            .iterator();
        if(!commits.hasNext()) continue;
        commitId = commits.next().getName();
      }
      tagsByCommit.computeIfAbsent(commitId, k -> new ArrayList<>()).add(tag.getName().substring(tagsPrefix.length()));
    }
    return tagsByCommit;
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
      this(repositoryPath, null);
    }

    public Builder(@NotNull File repositoryPath, @Nullable File workPath) {
      command = new LogsCommand(repositoryPath, workPath);
    }

    public Builder path(String path) {
      command.path = path;
      return this;
    }

    public Builder excludeDeletedCommits(boolean excludeDeletedCommits) {
      command.excludeDeletedCommits = excludeDeletedCommits;
      return this;
    }

    /**
     * Fill in the tags, under this prefix, of the commits. The tag names are given without the prefix.
     */
    public Builder tagsPrefix(String tagsPrefix) {
      command.tagsPrefix = tagsPrefix;
      return this;
    }

    public LogsCommand build() {
      return command;
    }
  }

}
