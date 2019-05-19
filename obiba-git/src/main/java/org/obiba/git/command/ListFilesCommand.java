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
import java.util.Collections;
import java.util.Set;
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

  private boolean recursive;

  private ListFilesCommand(File repositoryPath, @Nullable File workPath) {
    super(repositoryPath, workPath);
  }

  @Override
  public Set<String> execute(Git git) {
    Repository repository = git.getRepository();
    RevWalk walk = new RevWalk(repository);
    try {

      RevCommit commit = getRevCommit(repository, walk);
      if(commit == null) {
        // no commit yet
        return Collections.emptySet();
      }

      TreeWalk commitWalk = new TreeWalk(repository);
      commitWalk.addTree(commit.getTree());
      commitWalk.setRecursive(recursive);
      return findFiles(commitWalk);

    } catch(IOException e) {
      throw new GitException(e);
    }
  }

  private Set<String> findFiles(TreeWalk commitWalk) throws IOException {
    ImmutableSet.Builder<String> files = ImmutableSet.builder();
    if(Strings.isNullOrEmpty(filter)) {
      while(commitWalk.next()) {
        files.add(commitWalk.getPathString());
      }
    } else {
      Pattern pattern = Pattern.compile(filter);
      while(commitWalk.next()) {
        String filePath = commitWalk.getPathString();
        if(pattern.matcher(filePath).find()) {
          files.add(filePath);
        }
      }
    }
    return files.build();
  }

  @Nullable
  private RevCommit getRevCommit(Repository repository, RevWalk walk) throws IOException {

    String commitIdToFetch = Strings.isNullOrEmpty(commitId) ? GitUtils.getHeadCommitId(repository) : commitId;
    return Strings.isNullOrEmpty(commitIdToFetch) ? null : walk.parseCommit(ObjectId.fromString(commitIdToFetch));
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
