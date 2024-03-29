/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.shiro;

import java.util.Objects;

import org.apache.shiro.subject.Subject;
import org.obiba.shiro.realm.SudoRealm;

public class SessionStorageEvaluator implements org.apache.shiro.mgt.SessionStorageEvaluator {

  @Override
  public boolean isSessionStorageEnabled(Subject subject) {
    return subject != null && !Objects.equals(subject.getPrincipal(), SudoRealm.SudoPrincipal.INSTANCE);
  }

}
