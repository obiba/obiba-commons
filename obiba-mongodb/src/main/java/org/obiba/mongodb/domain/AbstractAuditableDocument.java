/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.mongodb.domain;

import java.util.Objects;

import org.joda.time.DateTime;
import org.springframework.data.annotation.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.domain.Persistable;

public abstract class AbstractAuditableDocument implements Persistable<String>, Timestamped {

  private static final long serialVersionUID = -5039056351334888684L;

  @Id
  private String id;

  @Version
  private Long version;

  @CreatedBy
  private String createdBy;

  @CreatedDate
  private DateTime createdDate = DateTime.now();

  @LastModifiedBy
  private String lastModifiedBy;

  @LastModifiedDate
  private DateTime lastModifiedDate;

  @Override
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

  @JsonIgnore
  @Override
  public boolean isNew() {
    return id == null;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  @Override
  public DateTime getCreatedDate() {
    return createdDate;
  }

  @Override
  public void setCreatedDate(DateTime createdDate) {
    this.createdDate = createdDate;
  }

  public String getLastModifiedBy() {
    return lastModifiedBy;
  }

  public void setLastModifiedBy(String lastModifiedBy) {
    this.lastModifiedBy = lastModifiedBy;
  }

  @Override
  public DateTime getLastModifiedDate() {
    return lastModifiedDate;
  }

  @Override
  public void setLastModifiedDate(DateTime lastModifiedDate) {
    this.lastModifiedDate = lastModifiedDate;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  @SuppressWarnings("SimplifiableIfStatement")
  public boolean equals(Object obj) {
    if(this == obj) return true;
    if(obj == null || getClass() != obj.getClass()) return false;
    return Objects.equals(id, ((AbstractAuditableDocument) obj).id);
  }

  @Override
  public String toString() {
    return "AbstractAuditableDocument{" +
        "id='" + id + '\'' +
        ", version=" + version +
        ", createdBy='" + createdBy + '\'' +
        ", createdDate=" + createdDate +
        ", lastModifiedBy='" + lastModifiedBy + '\'' +
        ", lastModifiedDate=" + lastModifiedDate +
        '}';
  }
}

