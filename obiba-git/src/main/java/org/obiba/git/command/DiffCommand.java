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

import jakarta.validation.constraints.NotNull;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.obiba.git.GitException;

import com.google.common.base.Strings;
import org.springframework.lang.Nullable;

/**
 * Opal GIT command used to extract the diff between two commits. By default, the diff is between the given commit and
 * its parent. By providing a valid 'nthCommit' value, the command will extract the appropriate diff from the repo.
 */
public class DiffCommand extends AbstractGitCommand<Iterable<DiffEntry>> {

  private String path;

  private final String commitId;

  private String previousCommitId;

  private int nthCommit = 1;

  protected DiffCommand(@NotNull File repositoryPath, @Nullable File workPath, String commitId) {
    super(repositoryPath, workPath);
    this.commitId = commitId;
  }

  @Override
  public Iterable<DiffEntry> execute(Git git) {

    Repository repository = git.getRepository();
    ObjectReader reader = repository.newObjectReader();
    try {
      DiffCurrentPreviousTreeParsersFactory parsersFactory = new DiffCurrentPreviousTreeParsersFactory(repository,
          reader);
      return compareDiffTrees(repository, parsersFactory);
    } catch(IOException e) {
      throw new GitException(e);
    } finally {
      reader.close();
    }
  }

  private Iterable<DiffEntry> compareDiffTrees(Repository repository,
      DiffCurrentPreviousTreeParsersFactory parsersFactory) throws IOException {

    DiffFormatter diffFormatter = new DiffFormatter(null);
    diffFormatter.setRepository(repository);
    diffFormatter.setDiffComparator(RawTextComparator.DEFAULT);
    diffFormatter.setDetectRenames(true);
    if(!Strings.isNullOrEmpty(path)) {
      diffFormatter.setPathFilter(PathFilter.create(path));
    }
    return diffFormatter.scan(parsersFactory.getPreviousCommitParser(), parsersFactory.getCurrentCommitParser());
  }

  private class DiffCurrentPreviousTreeParsersFactory {

    private final Repository repository;

    private final ObjectReader reader;

    private CanonicalTreeParser currentCommitParser;

    private AbstractTreeIterator previousCommitParser;

    private DiffCurrentPreviousTreeParsersFactory(Repository repository, ObjectReader reader) throws IOException {
      this.repository = repository;
      this.reader = reader;
      init();
    }

    private void init() throws IOException {
      RevCommit currentCommit = getCommitById(commitId);
      if(currentCommit == null) {
        throw new GitException(String.format("There are no commit with id '%s'", commitId));
      }

      currentCommitParser = new CanonicalTreeParser();
      currentCommitParser.reset(reader, currentCommit.getTree());

      RevCommit previousCommit = Strings.isNullOrEmpty(previousCommitId) //
          ? getCommitById(commitId + "~" + nthCommit) //
          : getCommitById(previousCommitId);

      if(previousCommit == null) {
        // currentCommit is the first commit in the tree
        previousCommitParser = new EmptyTreeIterator();
      } else {
        CanonicalTreeParser parser = new CanonicalTreeParser();
        parser.reset(reader, previousCommit.getTree());
        previousCommitParser = parser;
      }
    }

    public CanonicalTreeParser getCurrentCommitParser() {
      return currentCommitParser;
    }

    public AbstractTreeIterator getPreviousCommitParser() {
      return previousCommitParser;
    }

    @Nullable
    private RevCommit getCommitById(String id) throws IOException {
      ObjectId objectId = repository.resolve(id);
      return objectId == null ? null : new RevWalk(repository).parseCommit(objectId);
    }
  }

  @SuppressWarnings("ParameterHidesMemberVariable")
  public static class Builder {

    private final DiffCommand command;

    public Builder(@NotNull File repositoryPath, String commitId) {
      this(repositoryPath, null, commitId);
    }

    public Builder(@NotNull File repositoryPath, @Nullable File workPath, String commitId) {
      command = new DiffCommand(repositoryPath, workPath, commitId);
    }

    public Builder path(String path) {
      command.path = path;
      return this;
    }

    public Builder previousCommitId(String previousCommitId) {
      command.previousCommitId = previousCommitId;
      return this;
    }

    public Builder nthCommit(int nthCommit) {
      command.nthCommit = nthCommit;
      return this;
    }

    public DiffCommand build() {
      return command;
    }
  }

}
