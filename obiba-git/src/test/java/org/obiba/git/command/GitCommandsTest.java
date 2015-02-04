package org.obiba.git.command;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Scanner;
import java.util.Set;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obiba.core.util.FileUtil;
import org.obiba.git.CommitInfo;
import org.obiba.git.GitException;
import org.obiba.git.NoSuchGitRepositoryException;
import org.obiba.git.TagInfo;

import com.google.common.base.Charsets;
import com.google.common.collect.Iterables;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class GitCommandsTest {

  private final GitCommandHandler handler = new GitCommandHandler();

  private static File testFolder;

  @BeforeClass
  public static void init() {
    SecurityUtils.setSecurityManager(new DefaultSecurityManager());
    testFolder = Files.createTempDir();
  }

  @AfterClass
  public static void cleanup() {
    try {
      FileUtil.delete(testFolder);
    } catch(IOException e) {
    }
  }

  @Test
  @SuppressWarnings("ConstantConditions")
  public void test_create_read_files() throws Exception {

    File repo = getRepoPath();
    createDummyFiles(repo);

    assertThat(readFile(repo, "root.txt")).isEqualTo("This is root file");
    assertThat(readFile(repo, "dir/file.txt")).isEqualTo("This is a file in dir");

    Iterable<CommitInfo> commitInfos = handler.execute(new LogsCommand.Builder(repo).build());
    assertThat(commitInfos).hasSize(1);
    CommitInfo commitInfo = Iterables.getFirst(commitInfos, null);
    assertThat(commitInfo).isNotNull();
    assertThat(commitInfo.getAuthorName()).isEqualTo(AbstractGitWriteCommand.DEFAULT_AUTHOR_NAME);
    assertThat(commitInfo.getAuthorEmail()).isEqualTo(AbstractGitWriteCommand.DEFAULT_AUTHOR_EMAIL);
    assertThat(commitInfo.isHead()).isTrue();
    assertThat(commitInfo.isCurrent()).isTrue();
    assertThat(commitInfo.getComment()).isEqualTo("Initial commit");

    try {
      handler.execute(new ReadFileCommand.Builder(repo, "none").build());
      fail("Should throw GitException");
    } catch(GitException e) {
      assertThat(e).hasRootCauseExactlyInstanceOf(FileNotFoundException.class);
    }
  }

  @Test
  @SuppressWarnings("ConstantConditions")
  public void test_delete_files() throws Exception {

    File repo = getRepoPath();
    createDummyFiles(repo);

    handler.execute(new DeleteFilesCommand.Builder(repo, "dir", "Deleting dir folder.").build());
    assertThat(readFile(repo, "root.txt")).isEqualTo("This is root file");

    try {
      readFile(repo, "dir/file.txt");
    } catch(GitException e) {
      assertThat(e).hasRootCauseExactlyInstanceOf(FileNotFoundException.class);
    }

    handler.execute(new DeleteFilesCommand.Builder(repo, "root.txt", "Deleting root file").build());

    try {
      assertThat(readFile(repo, "root.txt"));
    } catch(GitException e) {
      assertThat(e).hasRootCauseExactlyInstanceOf(FileNotFoundException.class);
    }
  }

  @Test
  public void test_empty_commit_comment() throws Exception {
    File repo = getRepoPath();
    createDummyFiles(repo);

    try {
      handler.execute(new DeleteFilesCommand.Builder(repo, "dir", null).build());
    } catch(GitException e) {
      assertThat(e).hasRootCauseExactlyInstanceOf(NoMessageException.class);
    }

    try {
      handler.execute(new DeleteFilesCommand.Builder(repo, "dir", "").build());
    } catch(GitException e) {
      assertThat(e).hasRootCauseExactlyInstanceOf(NoMessageException.class);
    }

  }

  @Test
  public void test_exclude_file_delete_commits() throws Exception {
    File repo = getRepoPath();
    createDummyFiles(repo);

    handler.execute(new DeleteFilesCommand.Builder(repo, "dir", "Deleting files").build());

    Iterable<CommitInfo> commitInfos = handler
        .execute(new LogsCommand.Builder(repo).excludeDeletedCommits(true).build());
    assertThat(commitInfos).hasSize(1);
    CommitInfo commitInfo = Iterables.getFirst(commitInfos, null);
    assertThat(commitInfo).isNotNull();
    assertThat(commitInfo.getComment()).isEqualTo("Initial commit");
  }

  @Test(expected = NoSuchGitRepositoryException.class)
  public void test_read_files_from_invalid_repo() throws Exception {
    handler.execute(new ReadFileCommand.Builder(getRepoPath(), "file.txt").build());
  }

  @Test
  public void test_update_read_files() throws Exception {

    File repo = getRepoPath();

    try(InputStream input = new FileInputStream(createFile("Version 1"))) {
      handler.execute(new AddFilesCommand.Builder(repo, "First commit").addFile("root.txt", input).build());
    }
    try(InputStream input = new FileInputStream(createFile("Version 2"))) {
      handler.execute(new AddFilesCommand.Builder(repo, "Second commit").addFile("root.txt", input).build());
    }

    Iterable<CommitInfo> commitInfos = handler.execute(new LogsCommand.Builder(repo).build());
    assertThat(commitInfos).hasSize(2);

    assertThat(readFile(repo, "root.txt")).isEqualTo("Version 2");
  }

  @Test
  @SuppressWarnings("ConstantConditions")
  public void test_tags() throws Exception {

    File repo = getRepoPath();

    try(InputStream input = new FileInputStream(createFile("Version 1"))) {
      handler.execute(new AddFilesCommand.Builder(repo, "First commit").addFile("root.txt", input).build());
    }

    assertThat(handler.execute(new TagListCommand.Builder(repo).build())).isEmpty();

    handler.execute(new TagCommand.Builder(repo, "Test first tag", "1.0").build());

    Iterable<TagInfo> tagInfos = handler.execute(new TagListCommand.Builder(repo).build());
    assertThat(tagInfos).hasSize(1);
    TagInfo tagInfo = Iterables.getFirst(tagInfos, null);
    assertThat(tagInfo).isNotNull();
    assertThat(tagInfo.getName()).isEqualTo("1.0");
    assertThat(tagInfo.getCommitId()).isNotEmpty();

    assertThat(readFileFromTag(repo, "root.txt", "1.0")).isEqualTo("Version 1");
    assertThat(readFileFromCommit(repo, "root.txt", tagInfo.getCommitId())).isEqualTo("Version 1");
  }

  @Test
  public void test_list_files_of_empty_repo() throws Exception {
    File repo = getRepoPath();
    assertThat(handler.execute(new ListFilesCommand.Builder(repo).build())).isEmpty();
  }

  @Test
  public void test_list_files_no_filter_not_recursive() throws Exception {
    File repo = getRepoPath();
    createDummyFiles(repo);

    Collection<String> files = handler.execute(new ListFilesCommand.Builder(repo).build());
    assertThat(files.size()).isEqualTo(2);
    assertThat(files.contains("dir")).isTrue();
    assertThat(files.contains("root.txt")).isTrue();
  }

  @Test
  public void test_list_files_no_filter_recursive() throws Exception {
    File repo = getRepoPath();
    createDummyFiles(repo);

    Collection<String> files = handler.execute(new ListFilesCommand.Builder(repo).recursive(true).build());
    assertThat(files.size()).isEqualTo(5);
    assertThat(files.contains("dir/file.txt")).isTrue();
    assertThat(files.contains("root.txt")).isTrue();
  }

  @Test
  public void test_list_files_filtered_recursive() throws Exception {
    File repo = getRepoPath();
    createDummyFiles(repo);

    Collection<String> files = handler
        .execute(new ListFilesCommand.Builder(repo).recursive(true).filter("\\/_titi\\.txt|^root|\\.xml$").build());
    assertThat(files.size()).isEqualTo(3);
    assertThat(files.contains("dir/toto.xml")).isTrue();
    assertThat(files.contains("root.txt")).isTrue();
    assertThat(files.contains("dir/tata/_titi.txt")).isTrue();
  }

  @Test
  public void test_reading_files() throws Exception {
    File repo = getRepoPath();
    createDummyFiles(repo);

    Set<InputStream> files = handler
        .execute(new ReadFilesCommand.Builder(repo).recursive(true).filter("\\/_titi\\.txt|^root|\\.xml$").build());
    assertThat(files.size()).isEqualTo(3);

    for(InputStream is : files) {
      assertThat(readInputStream(is)).matches("This is another file in dir|This is root file|This is with folders");
    }
  }

  private void createDummyFiles(File repo) throws IOException {
    try(InputStream input1 = new FileInputStream(createFile("This is root file"));
        InputStream input2 = new FileInputStream(createFile("This is a file in dir"));
        InputStream input3 = new FileInputStream(createFile("This is another file in dir"));
        InputStream input4 = new FileInputStream(createFile("This is yet another file in dir"));
        InputStream input5 = new FileInputStream(createFile("This is with folders"))) {
      handler.execute(new AddFilesCommand.Builder(repo, "Initial commit") //
          .addFile("root.txt", input1) //
          .addFile("dir/file.txt", input2) //
          .addFile("dir/toto.xml", input3) //
          .addFile("dir/tata_titi.txt", input4) //
          .addFile("dir/tata/_titi.txt", input5) //
          .build());
    }
  }

  private File getRepoPath() throws IOException {
    File repo = File.createTempFile("obiba", ".git", testFolder);
    // delete it so we create a new repo
    if(!repo.delete()) {
      throw new IllegalStateException("Cannot delete git repo " + repo.getAbsolutePath());
    }
    return repo;
  }

  private String readFile(File repo, String path) throws Exception {
    return readFile(repo, path, null, null);
  }

  private String readFileFromTag(File repo, String path, String tag) throws Exception {
    return readFile(repo, path, null, tag);
  }

  private String readFileFromCommit(File repo, String path, String commitId) throws Exception {
    return readFile(repo, path, commitId, null);
  }

  private String readFile(File repo, String path, String commitId, String tag) throws Exception {
    InputStream inputStream = handler
        .execute(new ReadFileCommand.Builder(repo, path).commitId(commitId).tag(tag).build());
    return CharStreams.toString(new InputStreamReader(inputStream, Charsets.UTF_8));
  }

  private File createFile(CharSequence content) throws IOException {
    File file = File.createTempFile("obiba", "git", testFolder);
    file.deleteOnExit();
    Files.write(content, file, Charsets.UTF_8);
    return file;
  }

  private String readInputStream(InputStream is) {
    return new Scanner(is, "UTF-8").useDelimiter("\\A").next();
  }
}
