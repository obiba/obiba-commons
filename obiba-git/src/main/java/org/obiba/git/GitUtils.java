package org.obiba.git;

import java.io.IOException;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;

public class GitUtils {

  private static final String HEAD_COMMIT_ID = "HEAD";

  private GitUtils() {}

  public static boolean isHead(Repository repository, String commitId) throws IOException {
    return HEAD_COMMIT_ID.equals(commitId) || getHeadCommitId(repository).equals(commitId);
  }

  public static ObjectId getHeadCommit(Repository repository) throws IOException {
    return repository.resolve(HEAD_COMMIT_ID);
  }

  public static String getHeadCommitId(Repository repository) throws IOException {
    ObjectId id = getHeadCommit(repository);
    return id == null ? "" : id.getName();
  }

}
