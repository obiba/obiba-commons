/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.oidc.web;

import org.obiba.oidc.OIDCException;

public class Cookie {

  private String name;
  private String value;
  private int version = 0;
  private String comment;
  private String domain;
  private int maxAge = -1;
  private String path;
  private boolean secure;
  private boolean isHttpOnly = false;

  public Cookie(final String name, final String value) {
    if (name == null || name.length() == 0) {
      throw new OIDCException("Cookie name and value cannot be empty");
    }
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public int getMaxAge() {
    return maxAge;
  }

  public void setMaxAge(int maxAge) {
    this.maxAge = maxAge;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public boolean isSecure() {
    return secure;
  }

  public void setSecure(boolean secure) {
    this.secure = secure;
  }


  public boolean isHttpOnly() {
    return isHttpOnly;
  }

  public void setHttpOnly(boolean httpOnly) {
    isHttpOnly = httpOnly;
  }
}
