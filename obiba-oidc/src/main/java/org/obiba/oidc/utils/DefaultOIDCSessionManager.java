/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.oidc.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.nimbusds.oauth2.sdk.id.State;
import org.obiba.oidc.OIDCSession;
import org.obiba.oidc.OIDCSessionManager;

import java.util.concurrent.TimeUnit;

public class DefaultOIDCSessionManager implements OIDCSessionManager {

  private static final long DEFAULT_LIFESPAN = 60;

  private Cache<String, OIDCSession> cache;

  private long expireAfterWrite;

  public DefaultOIDCSessionManager() {
    this(DEFAULT_LIFESPAN);
  }

  public DefaultOIDCSessionManager(long expireAfterWrite) {
    setExpireAfterWrite(expireAfterWrite);
  }

  /**
   * Set the time in seconds after which a State will be evicted.
   *
   * @param expireAfterWrite
   */
  public void setExpireAfterWrite(long expireAfterWrite) {
    this.expireAfterWrite = expireAfterWrite;
  }

  @Override
  public void saveSession(OIDCSession session) {
    synchronized (this) {
      init();
      cache.put(session.getStateValue(), session);
    }
  }

  @Override
  public OIDCSession getSession(String state) {
    synchronized (this) {
      init();
      return cache.getIfPresent(state);
    }
  }

  @Override
  public boolean hasSession(String state) {
    synchronized (this) {
      init();
      return cache.getIfPresent(state) != null;
    }
  }

  private void init() {
    if (cache == null) {
       cache = CacheBuilder.newBuilder()
          .maximumSize(1000)
          .expireAfterWrite(expireAfterWrite, TimeUnit.SECONDS)
          .build();
    }
  }
}
