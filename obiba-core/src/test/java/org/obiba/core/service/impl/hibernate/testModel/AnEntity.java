/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.core.service.impl.hibernate.testModel;

import javax.persistence.Entity;

import org.obiba.core.domain.AbstractEntity;

@Entity
public class AnEntity extends AbstractEntity {

  private static final long serialVersionUID = -9167410848313046815L;

  private String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
