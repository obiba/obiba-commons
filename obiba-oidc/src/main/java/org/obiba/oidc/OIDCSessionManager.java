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

import com.nimbusds.oauth2.sdk.id.State;

/**
 * Manage the states that are part of the the OpenID Connect dance with the ID provider.
 */
public interface OIDCSessionManager {

  /**
   * Save session.
   *
   * @param session
   */
  void saveSession(OIDCSession session);

  /**
   * Check the state is the same for the remote client.
   * 
   * @param client Session identifier
   * @param state
   * @return
   */
  boolean checkState(String client, State state);

  /**
   * Get the session for the client.
   *
   * @param client
   * @return
   */
  OIDCSession getSession(String client);

  /**
   * Check there is a session for the client.
   *
   * @param client
   * @return
   */
  boolean hasSession(String client);
}
