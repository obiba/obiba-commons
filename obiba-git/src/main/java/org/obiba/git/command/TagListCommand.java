package org.obiba.git.command;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.obiba.git.GitException;
import org.obiba.git.TagInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TagListCommand extends AbstractGitCommand<Iterable<TagInfo>> {

  private static final Logger log = LoggerFactory.getLogger(TagListCommand.class);

  private TagListCommand(@NotNull File repositoryPath, @Nullable File workPath) {
    super(repositoryPath, workPath);
  }

  @Override
  public Iterable<TagInfo> execute(Git git) {
    try {
      Collection<TagInfo> tagInfos = new ArrayList<>();
      for(Ref ref : git.tagList().call()) {
        tagInfos.add(new TagInfo.Builder() //
            .ref(ref.getName()).commitId(ref.getObjectId().getName()) //
            .name(ref.getName().substring(ref.getName().lastIndexOf('/') + 1, ref.getName().length())) //
            .build());
      }
      return tagInfos;
    } catch(GitAPIException e) {
      throw new GitException(e);
    }
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

    public TagListCommand build() {
      return command;
    }
  }

}
