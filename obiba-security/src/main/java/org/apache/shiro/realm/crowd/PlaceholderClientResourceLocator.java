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
