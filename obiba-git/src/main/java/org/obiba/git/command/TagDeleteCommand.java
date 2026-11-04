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
import java.util.List;

import jakarta.validation.constraints.NotNull;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefDatabase;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.obiba.git.GitException;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import jakarta.annotation.Nullable;

/**
 * Delete from the remote repository the tags under a prefix, a folder of the tag names, and the tag named as the prefix.
 * The clones lose them when they next fetch, see {@link GitCommandHandler}.
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
      RefDatabase refDatabase = git.getRepository().getRefDatabase();
      List<Ref> refs = new ArrayList<>(refDatabase.getRefsByPrefix(Constants.R_TAGS + prefix));
      Ref tag = refDatabase.exactRef(Constants.R_TAGS + prefix.substring(0, prefix.length() - 1));
      if(tag != null) refs.add(tag);
      if(refs.isEmpty()) return List.of();
      Iterable<PushResult> results = git.push().setRemote("origin")
          .setRefSpecs(refs.stream().map(ref -> new RefSpec(":" + ref.getName())).toList()).call();
      checkDeleted(results);
      return results;
    } catch(IOException | GitAPIException e) {
      throw new GitException(e);
    }
  }

  /**
   * A rejected ref update is reported in the push result, it does not make the push fail.
   */
  private void checkDeleted(Iterable<PushResult> results) {
    List<String> rejected = new ArrayList<>();
    for(PushResult result : results) {
      for(RemoteRefUpdate update : result.getRemoteUpdates()) {
        RemoteRefUpdate.Status status = update.getStatus();
        if(status != RemoteRefUpdate.Status.OK && status != RemoteRefUpdate.Status.NON_EXISTING) {
          rejected.add(update.getRemoteName() + " (" + status + ")");
        }
      }
    }
    if(!rejected.isEmpty()) throw new GitException("Failed to delete tags: " + String.join(", ", rejected));
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
