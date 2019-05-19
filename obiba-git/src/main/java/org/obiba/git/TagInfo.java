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

import com.google.common.base.Objects;

public class TagInfo {

  private String name;

  private String ref;

  private String commitId;

  public String getName() {
    return name;
  }

  public String getRef() {
    return ref;
  }

  public String getCommitId() {
    return commitId;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("name", name).add("ref", ref).add("commitId", commitId).toString();
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

    public TagInfo build() {
      return tagInfo;
    }
  }

}
