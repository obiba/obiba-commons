/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.apache.shiro.realm.crowd;

import org.springframework.beans.factory.InitializingBean;

import com.atlassian.crowd.service.client.ClientResourceLocator;

/**
 *
 */
public class PlaceholderClientResourceLocator extends ClientResourceLocator implements InitializingBean {

  private String crowdPropertiesPath;

  public PlaceholderClientResourceLocator(String resourceName) {
    super(resourceName);
  }

  private String findPropertyFileLocation() {
    if(crowdPropertiesPath == null) {
      String location = getResourceLocationFromSystemProperty();
      if(location == null) {
        location = getResourceLocationFromClassPath();
      }
      return location;
    }
    return crowdPropertiesPath;
  }

  public void setCrowdPropertiesPath(String crowdPropertiesPath) {
    this.crowdPropertiesPath = crowdPropertiesPath;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    propertyFileLocation = findPropertyFileLocation();
  }
}
