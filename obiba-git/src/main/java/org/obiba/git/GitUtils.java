/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.git;

import java.io.IOException;
import java.util.Objects;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.springframework.lang.Nullable;

public class GitUtils {

  private static final String HEAD_COMMIT_ID = "HEAD";

  private GitUtils() {}

  @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
  public static boolean isHead(Repository repository, String commitId) throws IOException {
    return HEAD_COMMIT_ID.equals(commitId) || Objects.equals(getHeadCommitId(repository), commitId);
  }

  public static ObjectId getHeadCommit(Repository repository) throws IOException {
    return repository.resolve(HEAD_COMMIT_ID);
  }

  @Nullable
  public static String getHeadCommitId(Repository repository) throws IOException {
    ObjectId id = getHeadCommit(repository);
    return id == null ? null : id.getName();
  }

}
