package org.obiba.git.command;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.validation.constraints.NotNull;

import org.obiba.git.GitException;

public class ReadFileCommand extends AbstractGitReadCommand<InputStream> implements GitReadCommand<InputStream> {

  private final String pathInRepo;

  private ReadFileCommand(@NotNull File repositoryPath, String pathInRepo) {
    super(repositoryPath);
    this.pathInRepo = pathInRepo;
  }

  @Override
  public InputStream execute(File repository) {
    try {
      return new FileInputStream(new File(repository, pathInRepo));
    } catch(FileNotFoundException e) {
      throw new GitException(e);
    }
  }

  public static class Builder {

    private final ReadFileCommand command;

    public Builder(@NotNull File repositoryPath, String pathInRepo) {
      command = new ReadFileCommand(repositoryPath, pathInRepo);
    }

    public ReadFileCommand build() {
      return command;
    }
  }
}
