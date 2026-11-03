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

    // the tagged commit, not the annotated tag object
    assertThat(tagInfo.getCommitId()).isEqualTo(getCommitIds(repo, null)[0]);
    assertThat(tagInfo.getMessage()).isEqualTo("Test first tag");
    assertThat(tagInfo.getTaggerName()).isEqualTo(AbstractGitWriteCommand.DEFAULT_AUTHOR_NAME);
    assertThat(tagInfo.getDate()).isNotNull();
  }

  @Test
  public void test_tag_path() throws Exception {
    File repo = getRepoPath();
    addFile(repo, "a.txt", "A1", "Add a");
    addFile(repo, "b.txt", "B1", "Add b");

    handler.execute(new TagCommand.Builder(repo, "Tag a", "t1").path("a.txt").build());

    TagInfo tagInfo = Iterables.getOnlyElement(handler.execute(new TagListCommand.Builder(repo).build()));
    assertThat(tagInfo.getCommitId()).isEqualTo(getCommitIds(repo, "a.txt")[0]);
    assertThat(tagInfo.getCommitId()).isNotEqualTo(getCommitIds(repo, null)[0]);
  }

  @Test
  public void test_tag_path_without_commit() throws Exception {
    File repo = getRepoPath();
    addFile(repo, "a.txt", "A1", "Add a");
    try {
      handler.execute(new TagCommand.Builder(repo, "Tag", "t1").path("none.txt").build());
      fail("Should throw GitException");
    } catch(GitException e) {
      assertThat(e).hasMessageContaining("none.txt");
    }
  }

  @Test
  public void test_tag_rejected_names() throws Exception {
    File repo = getRepoPath();
    addFile(repo, "a.txt", "A1", "Add a");
    handler.execute(new TagCommand.Builder(repo, "Tag", "t1").build());

    for(String name : new String[] { "t1", "bad name", "bad..name" }) {
      try {
        handler.execute(new TagCommand.Builder(repo, "Tag", name).build());
        fail("Should throw GitException for tag name: " + name);
      } catch(GitException e) {
        // expected
      }
    }
    assertThat(handler.execute(new TagListCommand.Builder(repo).build())).hasSize(1);
  }

  @Test
  public void test_tag_list_prefix() throws Exception {
    File repo = getRepoPath();
    addFile(repo, "a.txt", "A1", "Add a");
    handler.execute(new TagCommand.Builder(repo, "Tag", "v1/t1").build());
    handler.execute(new TagCommand.Builder(repo, "Tag", "v2/t2").build());

    TagInfo tagInfo = Iterables
        .getOnlyElement(handler.execute(new TagListCommand.Builder(repo).prefix("v1/").build()));
    // the full name, with slashes
    assertThat(tagInfo.getName()).isEqualTo("v1/t1");
    assertThat(tagInfo.getRef()).isEqualTo("refs/tags/v1/t1");
    assertThat(handler.execute(new TagListCommand.Builder(repo).build())).hasSize(2);
  }

  @Test
  public void test_logs_with_tags() throws Exception {
    File repo = getRepoPath();
    addFile(repo, "a.txt", "A1", "Add a"); // c1
    handler.execute(new TagCommand.Builder(repo, "Tag", "p/t0").build()); // on c1, before b.txt exists
    addFile(repo, "b.txt", "B1", "Add b"); // c2
    handler.execute(new TagCommand.Builder(repo, "Tag", "p/t1").build()); // on c2
    handler.execute(new TagCommand.Builder(repo, "Tag", "other").build()); // not under the prefix
    addFile(repo, "a.txt", "A2", "Update a"); // c3

    // folder (whole repository) history: tags on the tagged commits
    CommitInfo[] all = Iterables.toArray(handler.execute(new LogsCommand.Builder(repo).tagsPrefix("p/").build()),
        CommitInfo.class);
    assertThat(all).hasSize(3);
    assertThat(all[0].getTags()).isEmpty();
    assertThat(all[1].getTags()).containsExactly("t1");
    assertThat(all[2].getTags()).containsExactly("t0");

    // a.txt was not changed by c2: t1 goes on c1, the version of a.txt at the tag time
    CommitInfo[] a = Iterables
        .toArray(handler.execute(new LogsCommand.Builder(repo).path("a.txt").tagsPrefix("p/").build()),
            CommitInfo.class);
    assertThat(a).hasSize(2);
    assertThat(a[0].getTags()).isEmpty();
    assertThat(a[1].getTags()).containsExactlyInAnyOrder("t0", "t1");

    // b.txt did not exist at t0: t0 is left out
    CommitInfo[] b = Iterables
        .toArray(handler.execute(new LogsCommand.Builder(repo).path("b.txt").tagsPrefix("p/").build()),
            CommitInfo.class);
    assertThat(b).hasSize(1);
    assertThat(b[0].getTags()).containsExactly("t1");

    // no prefix, no tags
    assertThat(Iterables.getFirst(handler.execute(new LogsCommand.Builder(repo).path("b.txt").build()), null)
        .getTags()).isEmpty();
  }

  private void addFile(File repo, String path, String content, String message) throws IOException {
    try(InputStream input = new FileInputStream(createFile(content))) {
      handler.execute(new AddFilesCommand.Builder(repo, message).addFile(path, input).build());
    }
  }

  /**
   * Commit ids of the path, latest first.
   */
  private String[] getCommitIds(File repo, String path) {
    return Iterables.toArray(Iterables.transform(handler.execute(new LogsCommand.Builder(repo).path(path).build()),
        CommitInfo::getCommitId), String.class);
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
