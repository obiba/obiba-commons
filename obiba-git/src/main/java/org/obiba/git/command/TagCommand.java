package org.obiba.git.command;

import java.io.File;

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

  private TagCommand(@NotNull File repositoryPath, String message, String tagName) {
    super(repositoryPath, message);
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
      command = new TagCommand(repositoryPath, message, tagName);
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
