/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.oidc;

import java.util.Collection;

/**
 * Interface to query for OpenID Connect configurations.
 */
public interface OIDCConfigurationProvider {

  /**
   * Get the OIDC configurations living in the application.
   *
   * @return
   */
  Collection<OIDCConfiguration> getConfigurations();

  /**
   * Get the OIDC configuration from name.
   *
   * @return
   */
  OIDCConfiguration getConfiguration(String name);

}
