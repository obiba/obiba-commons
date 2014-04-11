package org.obiba.git.command;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.validation.constraints.NotNull;

public class ReadFileCommand extends AbstractGitReadCommand<InputStream> implements GitReadCommand<InputStream> {

  private final String pathInRepo;

  private ReadFileCommand(@NotNull File repositoryPath, String pathInRepo) {
    super(repositoryPath);
    this.pathInRepo = pathInRepo;
  }

  @Override
  public InputStream execute(File repository) throws Exception {
    return new FileInputStream(new File(repository, pathInRepo));
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
