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
import java.util.Iterator;

import jakarta.validation.constraints.NotNull;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.PushResult;
import org.obiba.git.GitException;
import com.google.common.base.Strings;
import jakarta.annotation.Nullable;

@SuppressWarnings("ClassTooDeepInInheritanceTree")
public class TagCommand extends AbstractGitWriteCommand {

  private final String tagName;

  private boolean signed;

  private String path;

  private TagCommand(@NotNull File repositoryPath, @Nullable File workPath, String message, String tagName) {
    super(repositoryPath, workPath, message);
    this.tagName = tagName;
  }

  @Override
  public Iterable<PushResult> execute(Git git) {
    try {
      // an existing or invalid tag name is rejected by jgit
      org.eclipse.jgit.api.TagCommand tagCommand = git.tag().setMessage(getCommitMessage()).setName(tagName)
          .setSigned(signed).setTagger(new PersonIdent(getAuthorName(), getAuthorEmail()));
      if(!Strings.isNullOrEmpty(path)) {
        Iterator<RevCommit> commits = git.log().addPath(path).setMaxCount(1).call().iterator();
        if(!commits.hasNext()) throw new GitException("No commit for path: " + path);
        tagCommand.setObjectId(commits.next());
      }
      tagCommand.call();
      return git.push().setPushTags().setRemote("origin").call();
    } catch(GitAPIException e) {
      throw new GitException(e);
    }
  }

  @SuppressWarnings("ParameterHidesMemberVariable")
  public static class Builder {

    private final TagCommand command;

    public Builder(@NotNull File repositoryPath, String message, String tagName) {
      this(repositoryPath, null, message, tagName);
    }

    public Builder(@NotNull File repositoryPath, @Nullable File workPath, String message, String tagName) {
      command = new TagCommand(repositoryPath, workPath, message, tagName);
    }

    public Builder signed(boolean signed) {
      command.signed = signed;
      return this;
    }

    /**
     * Tag the latest commit of this path instead of HEAD.
     */
    public Builder path(String path) {
      command.path = path;
      return this;
    }

    public TagCommand build() {
      return command;
    }
  }
}
