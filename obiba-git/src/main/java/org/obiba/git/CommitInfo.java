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

import org.springframework.lang.Nullable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

public class CommitInfo {

  private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");

  static {
    DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  private String authorName;

  private String authorEmail;

  private Date date;

  private String comment;

  private String commitId;

  private List<String> diffEntries;

  private String blob;

  private boolean head;

  private boolean current;

  public String getAuthorName() {
    return authorName;
  }

  public String getAuthorEmail() {
    return authorEmail;
  }

  public String getComment() {
    return comment;
  }

  public String getCommitId() {
    return commitId;
  }

  public String getBlob() {
    return blob;
  }

  public boolean isHead() {
    return head;
  }

  public boolean isCurrent() {
    return current;
  }

  public Date getDate() {
    return (Date) date.clone();
  }

  public String getDateAsIso8601() {
    return DATE_FORMAT.format(date);
  }

  @Nullable
  public List<String> getDiffEntries() {
    return diffEntries == null ? null : diffEntries.subList(0, diffEntries.size());
  }

  @Override
  public String toString() {
    return "CommitInfo{" +
        "authorName='" + authorName + '\'' +
        ", authorEmail='" + authorEmail + '\'' +
        ", date=" + date +
        ", comment='" + comment + '\'' +
        ", commitId='" + commitId + '\'' +
        ", diffEntries=" + diffEntries +
        ", blob='" + blob + '\'' +
        ", head=" + head +
        ", current=" + current +
        '}';
  }

  @Override
  public int hashCode() {
    return Objects.hash(commitId);
  }

  @Override
  @SuppressWarnings("SimplifiableIfStatement")
  public boolean equals(Object obj) {
    if(this == obj) return true;
    if(obj == null || getClass() != obj.getClass()) return false;
    return Objects.equals(commitId, ((CommitInfo) obj).commitId);
  }

  @SuppressWarnings("ParameterHidesMemberVariable")
  public static class Builder {

    private final CommitInfo commitInfo = new CommitInfo();

    public Builder authorName(String authorName) {
      commitInfo.authorName = authorName;
      return this;
    }

    public Builder authorEmail(String authorEmail) {
      commitInfo.authorEmail = authorEmail;
      return this;
    }

    @SuppressWarnings("AssignmentToDateFieldFromParameter")
    public Builder date(Date date) {
      commitInfo.date = date;
      return this;
    }

    public Builder comment(String comment) {
      commitInfo.comment = comment;
      return this;
    }

    public Builder commitId(String commitId) {
      commitInfo.commitId = commitId;
      return this;
    }

    public Builder blob(String blob) {
      commitInfo.blob = blob;
      return this;
    }

    public Builder head(boolean head) {
      commitInfo.head = head;
      return this;
    }

    public Builder current(boolean current) {
      commitInfo.current = current;
      return this;
    }

    public Builder diffEntries(List<String> diffEntries) {
      commitInfo.diffEntries = diffEntries;
      return this;
    }

    public static Builder createFromObject(CommitInfo commitInfo) {
      return new Builder().authorName(commitInfo.authorName).authorEmail(commitInfo.authorEmail)
          .comment(commitInfo.comment).commitId(commitInfo.commitId).date(commitInfo.date)
          .diffEntries(commitInfo.diffEntries);
    }

    public CommitInfo build() {
      return commitInfo;
    }
  }
}
