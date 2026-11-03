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

import java.util.Date;

import jakarta.annotation.Nullable;

public class TagInfo {

  private String name;

  private String ref;

  private String commitId;

  private String message;

  private String taggerName;

  private String taggerEmail;

  private Date date;

  /**
   * Full tag name, after refs/tags/.
   */
  public String getName() {
    return name;
  }

  public String getRef() {
    return ref;
  }

  /**
   * Id of the tagged commit.
   */
  public String getCommitId() {
    return commitId;
  }

  /**
   * Message of an annotated tag, null for a lightweight tag.
   */
  @Nullable
  public String getMessage() {
    return message;
  }

  @Nullable
  public String getTaggerName() {
    return taggerName;
  }

  @Nullable
  public String getTaggerEmail() {
    return taggerEmail;
  }

  @Nullable
  public Date getDate() {
    return date == null ? null : (Date) date.clone();
  }

  @Override
  public String toString() {
    return "TagInfo{" +
        "name='" + name + '\'' +
        ", ref='" + ref + '\'' +
        ", commitId='" + commitId + '\'' +
        ", message='" + message + '\'' +
        ", taggerName='" + taggerName + '\'' +
        ", taggerEmail='" + taggerEmail + '\'' +
        ", date=" + date +
        '}';
  }

  @SuppressWarnings("ParameterHidesMemberVariable")
  public static class Builder {

    private final TagInfo tagInfo = new TagInfo();

    public Builder name(String name) {
      tagInfo.name = name;
      return this;
    }

    public Builder ref(String ref) {
      tagInfo.ref = ref;
      return this;
    }

    public Builder commitId(String commitId) {
      tagInfo.commitId = commitId;
      return this;
    }

    public Builder message(String message) {
      tagInfo.message = message;
      return this;
    }

    public Builder taggerName(String taggerName) {
      tagInfo.taggerName = taggerName;
      return this;
    }

    public Builder taggerEmail(String taggerEmail) {
      tagInfo.taggerEmail = taggerEmail;
      return this;
    }

    @SuppressWarnings("AssignmentToDateFieldFromParameter")
    public Builder date(Date date) {
      tagInfo.date = date;
      return this;
    }

    public TagInfo build() {
      return tagInfo;
    }
  }

}
