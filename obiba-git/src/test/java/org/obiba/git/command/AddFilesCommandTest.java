package org.obiba.git.command;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import static org.assertj.core.api.Assertions.assertThat;

public class AddFilesCommandTest {

  private static final Logger log = LoggerFactory.getLogger(AddFilesCommandTest.class);

  private final GitCommandHandler handler = new GitCommandHandler();

  @BeforeClass
  public static void init() {
    SecurityUtils.setSecurityManager(new DefaultSecurityManager());
  }

  @Test
  public void test_add_files_to_new_repo() throws Exception {

    File repo = File.createTempFile("obiba", "git");
    // delete it so we create a new repo
    if(!repo.delete()) {
      throw new IllegalStateException("Cannot delete git repo " + repo.getAbsolutePath());
    }

    File file1 = createFile("This is root file");
    File file2 = createFile("This is a file in dir");

    try(InputStream input1 = new FileInputStream(file1);
        InputStream input2 = new FileInputStream(file2)) {
      AddFilesCommand addFilesCommand = new AddFilesCommand.Builder(repo, "Initial commit") //
          .addFile("root.txt", input1) //
          .addFile("dir/file.txt", input2).build();
      handler.execute(addFilesCommand);
    }

    assertThat(new File(repo, "root.txt")).exists().isFile().hasContent("This is root file");
    assertThat(new File(repo, "dir/file.txt")).exists().isFile().hasContent("This is a file in dir");
  }

  private File createFile(CharSequence content) throws IOException {
    File file = File.createTempFile("obiba", "git");
    file.deleteOnExit();
    Files.write(content, file, Charsets.UTF_8);
    return file;
  }

}
