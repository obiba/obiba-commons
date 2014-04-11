package org.obiba.git.command;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obiba.git.NoSuchGitRepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;

import static org.assertj.core.api.Assertions.assertThat;

public class GitCommandsTest {

  private static final Logger log = LoggerFactory.getLogger(GitCommandsTest.class);

  private final GitCommandHandler handler = new GitCommandHandler();

  @BeforeClass
  public static void init() {
    SecurityUtils.setSecurityManager(new DefaultSecurityManager());
  }

  @Test
  public void test_write_read_files_in_new_repo() throws Exception {

    File repo = getRepoPath();
    File file1 = createFile("This is root file");
    File file2 = createFile("This is a file in dir");

    try(InputStream input1 = new FileInputStream(file1);
        InputStream input2 = new FileInputStream(file2)) {
      AddFilesCommand addFilesCommand = new AddFilesCommand.Builder(repo, "Initial commit") //
          .addFile("root.txt", input1) //
          .addFile("dir/file.txt", input2).build();
      handler.execute(addFilesCommand);
    }

    assertThat(readFile(repo, "root.txt")).isEqualTo("This is root file");
    assertThat(readFile(repo, "dir/file.txt")).isEqualTo("This is a file in dir");
  }

  @Test(expected = NoSuchGitRepositoryException.class)
  public void test_read_files_from_invalid_repo() throws Exception {
    handler.execute(new ReadFileCommand.Builder(getRepoPath(), "file.txt").build());
  }

  private File getRepoPath() throws IOException {
    File repo = File.createTempFile("obiba", "git");
    // delete it so we create a new repo
    if(!repo.delete()) {
      throw new IllegalStateException("Cannot delete git repo " + repo.getAbsolutePath());
    }
    return repo;
  }

  private String readFile(File repo, String path) throws IOException {
    InputStream inputStream = handler.execute(new ReadFileCommand.Builder(repo, path).build());
    return CharStreams.toString(new InputStreamReader(inputStream, Charsets.UTF_8));
  }

  private File createFile(CharSequence content) throws IOException {
    File file = File.createTempFile("obiba", "git");
    file.deleteOnExit();
    Files.write(content, file, Charsets.UTF_8);
    return file;
  }
}
