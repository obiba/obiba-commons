/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.runtime.upgrade;

import javax.sql.DataSource;

import org.obiba.runtime.Version;
import org.springframework.jdbc.core.JdbcTemplate;

public abstract class AbstractDatabaseUpgradeStep extends AbstractUpgradeStep {

  private DataSource dataSource;

  public DataSource getDataSource() {
    return dataSource;
  }

  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  protected abstract void execute(Version currentVersion, JdbcTemplate template);

  @Override
  public void execute(Version currentVersion) {
    JdbcTemplate template = new JdbcTemplate(dataSource);
    execute(currentVersion, template);
  }
}
