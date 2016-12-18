/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.runtime.jdbc;

import org.springframework.util.PatternMatchUtils;

@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD",
    justification = "XML deserialization")
public class DatabaseProduct {

  public String[] databaseProductNames;

  public String normalizedName;

  public String getNormalizedName() {
    return normalizedName;
  }

  public boolean isForProductName(String dbProductName) {
    return PatternMatchUtils.simpleMatch(databaseProductNames, dbProductName);
  }

  @Override
  public String toString() {
    return getNormalizedName();
  }

}
