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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.obiba.git.command.GitCommandTestUtils.createFile;

public class ReadFileCommandTest {

  private static final Logger log = LoggerFactory.getLogger(ReadFileCommandTest.class);

  private final GitCommandHandler handler = new GitCommandHandler();

  @BeforeClass
  public static void init() {
    SecurityUtils.setSecurityManager(new DefaultSecurityManager());
  }

  @Test
  public void test_read_files() throws Exception {

    File repo = File.createTempFile("obiba", "git");
    // delete it so we create a new repo
    if(!repo.delete()) {
      throw new IllegalStateException("Cannot delete git repo " + repo.getAbsolutePath());
    }

//    File repo = new File("target/repo.git");

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

  private String readFile(File repo, String path) throws IOException {
    InputStream inputStream = handler.execute(new ReadFileCommand.Builder(repo, path).build());
    return CharStreams.toString(new InputStreamReader(inputStream, Charsets.UTF_8));
  }

}
