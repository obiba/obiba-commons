package org.obiba.git.command;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.PushResult;
import org.obiba.git.GitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("ClassTooDeepInInheritanceTree")
public class AddDeleteFilesCommand extends AbstractGitWriteCommand {

  private static final Logger log = LoggerFactory.getLogger(AddDeleteFilesCommand.class);

  private final Collection<FileDescriptor> filesToAdd = new ArrayList<>();

  private final Collection<String> filePatternsToDelete = new ArrayList<>();

  private AddDeleteFilesCommand(@NotNull File repositoryPath, @Nullable File workPath, String commitMessage) {
    super(repositoryPath, workPath, commitMessage);
  }

  @Override
  public Iterable<PushResult> execute(Git git) {
    try {
      for(FileDescriptor file : filesToAdd) {
        Path path = Paths.get(git.getRepository().getWorkTree().getAbsolutePath(), file.getPathInRepo());
        //noinspection ResultOfMethodCallIgnored
        path.toFile().mkdirs();
        log.debug("Copy file to {}", path);
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
      }
      git.add().addFilepattern(".").call();
      for(String filePattern : filePatternsToDelete) {
        git.rm().addFilepattern(filePattern).call();
      }
      return commitAndPush(git);

    } catch(IOException | GitAPIException e) {
      throw new GitException(e);
    }
  }

  public static class FileDescriptor {

    private final String pathInRepo;

    private final InputStream inputStream;

    public FileDescriptor(String pathInRepo, InputStream inputStream) {
      this.pathInRepo = pathInRepo;
      this.inputStream = inputStream;
    }

    public String getPathInRepo() {
      return pathInRepo;
    }

    public InputStream getInputStream() {
      return inputStream;
    }
  }

  public static class Builder {

    private final AddDeleteFilesCommand command;

    public Builder(@NotNull File repositoryPath, String commitMessage) {
      this(repositoryPath, null, commitMessage);
    }

    public Builder(@NotNull File repositoryPath, @Nullable File workPath, String commitMessage) {
      command = new AddDeleteFilesCommand(repositoryPath, workPath, commitMessage);
    }

    public Builder addFile(FileDescriptor fileDescriptor) {
      command.filesToAdd.add(fileDescriptor);
      return this;
    }

    public Builder addFile(String pathInRepo, InputStream inputStream) {
      command.filesToAdd.add(new FileDescriptor(pathInRepo, inputStream));
      return this;
    }

    public Builder deleteFile(String filePattern) {
      command.filePatternsToDelete.add(filePattern);
      return this;
    }

    public AddDeleteFilesCommand build() {
      return command;
    }
  }
}
