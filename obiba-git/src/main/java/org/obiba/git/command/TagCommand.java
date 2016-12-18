/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.git.command;

import java.io.File;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.transport.PushResult;
import org.obiba.git.GitException;

@SuppressWarnings("ClassTooDeepInInheritanceTree")
public class TagCommand extends AbstractGitWriteCommand {

  private final String tagName;

  private boolean signed;

  private TagCommand(@NotNull File repositoryPath, @Nullable File workPath, String message, String tagName) {
    super(repositoryPath, workPath, message);
    this.tagName = tagName;
  }

  @Override
  public Iterable<PushResult> execute(Git git) {
    try {
      git.tag().setMessage(getCommitMessage()).setName(tagName).setSigned(signed)
          .setTagger(new PersonIdent(getAuthorName(), getAuthorEmail())).call();
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

    public TagCommand build() {
      return command;
    }
  }
}
