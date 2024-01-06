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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;

import jakarta.validation.constraints.NotNull;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.PushResult;
import org.obiba.git.GitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

@SuppressWarnings("ClassTooDeepInInheritanceTree")
public class AddFilesCommand extends AbstractGitWriteCommand {

  private static final Logger log = LoggerFactory.getLogger(AddFilesCommand.class);

  private final Collection<FileDescriptor> files = new ArrayList<>();

  private AddFilesCommand(@NotNull File repositoryPath, @Nullable File workPath, String commitMessage) {
    super(repositoryPath, workPath, commitMessage);
  }

  @Override
  public Iterable<PushResult> execute(Git git) {
    try {
      for(FileDescriptor file : files) {
        Path path = Paths.get(git.getRepository().getWorkTree().getAbsolutePath(), file.getPathInRepo());
        //noinspection ResultOfMethodCallIgnored
        path.toFile().mkdirs();
        log.debug("Copy file to {}", path);
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
      }
      git.add().addFilepattern(".").call();
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

    private final AddFilesCommand command;

    public Builder(@NotNull File repositoryPath, String commitMessage) {
      this(repositoryPath, null, commitMessage);
    }

    public Builder(@NotNull File repositoryPath, @Nullable File workPath, String commitMessage) {
      command = new AddFilesCommand(repositoryPath, workPath, commitMessage);
    }

    public Builder addFile(FileDescriptor fileDescriptor) {
      command.files.add(fileDescriptor);
      return this;
    }

    public Builder addFile(String pathInRepo, InputStream inputStream) {
      command.files.add(new FileDescriptor(pathInRepo, inputStream));
      return this;
    }

    public AddFilesCommand build() {
      return command;
    }
  }
}
