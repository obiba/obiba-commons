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
import java.util.Date;
import java.util.List;

import jakarta.validation.constraints.NotNull;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.obiba.git.GitException;
import org.obiba.git.TagInfo;

import com.google.common.base.Strings;
import jakarta.annotation.Nullable;

public class TagListCommand extends AbstractGitCommand<Iterable<TagInfo>> {

  private String prefix;

  private TagListCommand(@NotNull File repositoryPath, @Nullable File workPath) {
    super(repositoryPath, workPath);
  }

  @Override
  public Iterable<TagInfo> execute(Git git) {
    try {
      return listTags(git.getRepository(), prefix);
    } catch(IOException e) {
      throw new GitException(e);
    }
  }

  /**
   * List the tags whose name starts with the prefix (all the tags if the prefix is empty).
   * The commit id is the one of the tagged commit, also for an annotated tag.
   */
  static List<TagInfo> listTags(Repository repository, @Nullable String prefix) throws IOException {
    List<TagInfo> tagInfos = new ArrayList<>();
    try(RevWalk walk = new RevWalk(repository)) {
      for(Ref ref : repository.getRefDatabase().getRefsByPrefix(Constants.R_TAGS + Strings.nullToEmpty(prefix))) {
        RevObject object = walk.parseAny(ref.getObjectId());
        TagInfo.Builder builder = new TagInfo.Builder() //
            .ref(ref.getName()) //
            .name(ref.getName().substring(Constants.R_TAGS.length())) //
            .commitId(walk.peel(object).getName());
        if(object instanceof RevTag tag) {
          builder.message(tag.getFullMessage());
          PersonIdent tagger = tag.getTaggerIdent();
          if(tagger != null) {
            builder.taggerName(tagger.getName()).taggerEmail(tagger.getEmailAddress())
                .date(Date.from(tagger.getWhenAsInstant()));
          }
        }
        tagInfos.add(builder.build());
      }
    }
    return tagInfos;
  }

  @SuppressWarnings("ParameterHidesMemberVariable")
  public static class Builder {

    private final TagListCommand command;

    public Builder(@NotNull File repositoryPath) {
      this(repositoryPath, null);
    }

    public Builder(@NotNull File repositoryPath, @Nullable File workPath) {
      command = new TagListCommand(repositoryPath, workPath);
    }

    /**
     * List only the tags whose name starts with this prefix.
     */
    public Builder prefix(String prefix) {
      command.prefix = prefix;
      return this;
    }

    public TagListCommand build() {
      return command;
    }
  }

}
