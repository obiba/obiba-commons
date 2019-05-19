/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.shiro.authc;

import org.apache.shiro.authc.AuthenticationToken;

public class TicketAuthenticationToken implements AuthenticationToken {

  private static final long serialVersionUID = -926878906877007801L;

  private final String ticketId;

  private final String url;

  private final String hash;

  public TicketAuthenticationToken(String ticketId, String url, String hash) {
    this.ticketId = ticketId;
    this.url = url;
    this.hash = hash;
  }

  @Override
  public Object getPrincipal() {
    return getTicketId();
  }

  @Override
  public Object getCredentials() {
    return getHash();
  }

  public String getTicketId() {
    return ticketId;
  }

  public String getUrl() {
    return url;
  }

  public String getHash() {
    return hash;
  }

}
