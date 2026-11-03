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
import java.util.List;

import jakarta.validation.constraints.NotNull;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.obiba.git.GitException;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import jakarta.annotation.Nullable;

/**
 * Delete the tags under a prefix, a folder of the tag names, from the remote repository and from the clone. The clone
 * must also lose them: a fetch does not prune the tags and {@link TagCommand} pushes all the tags of the clone.
 */
@SuppressWarnings("ClassTooDeepInInheritanceTree")
public class TagDeleteCommand extends AbstractGitWriteCommand {

  private final String prefix;

  private TagDeleteCommand(@NotNull File repositoryPath, @Nullable File workPath, String prefix) {
    super(repositoryPath, workPath, null);
    Preconditions.checkArgument(!Strings.isNullOrEmpty(prefix), "prefix cannot be null or empty");
    this.prefix = TagListCommand.toFolderPrefix(prefix);
  }

  @Override
  public Iterable<PushResult> execute(Git git) {
    try {
      List<Ref> refs = git.getRepository().getRefDatabase().getRefsByPrefix(Constants.R_TAGS + prefix);
      if(refs.isEmpty()) return List.of();
      Iterable<PushResult> results = git.push().setRemote("origin")
          .setRefSpecs(refs.stream().map(ref -> new RefSpec(":" + ref.getName())).toList()).call();
      git.tagDelete().setTags(refs.stream().map(Ref::getName).toArray(String[]::new)).call();
      return results;
    } catch(IOException | GitAPIException e) {
      throw new GitException(e);
    }
  }

  public static class Builder {

    private final TagDeleteCommand command;

    public Builder(@NotNull File repositoryPath, String prefix) {
      this(repositoryPath, null, prefix);
    }

    public Builder(@NotNull File repositoryPath, @Nullable File workPath, String prefix) {
      command = new TagDeleteCommand(repositoryPath, workPath, prefix);
    }

    public TagDeleteCommand build() {
      return command;
    }
  }
}
