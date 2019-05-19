/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.wicket.util.seed;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.wicket.protocol.http.WebApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.transaction.annotation.Transactional;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;

@SuppressWarnings("UnusedDeclaration")
public class XstreamResourceDatabaseSeed implements DatabaseSeed, InitializingBean, ResourceLoaderAware {

  private static final Logger log = LoggerFactory.getLogger(XstreamResourceDatabaseSeed.class);

  private static final String RESOURCE_ENCODING = "ISO-8859-1";

  private final XStream xstream = new XStream();

  private Resource xstreamResource;

  private String[] xstreamResourcePatterns;

  private ResourcePatternResolver resolver;

  @Nullable
  private Map<String, Class<?>> aliases;

  private boolean developmentSeed = false;

  @Override
  @Transactional
  public void seedDatabase(WebApplication application) {
    if(!shouldSeed(application)) {
      return;
    }

    if(xstreamResource != null && xstreamResource.exists()) {
      Object result = handleXtreamResource(xstreamResource);
      handleXstreamResult(result);
    }

    if(xstreamResourcePatterns != null) {
      for(String locationPattern : xstreamResourcePatterns) {
        try {
          Resource[] resources = resolver.getResources(locationPattern);
          if(resources != null) {
            for(Resource resource : resources) {
              Object result = handleXtreamResource(resource);
              handleXstreamResult(resource, result);
            }
          }
        } catch(IOException e) {
          log.error("Error resolving resource pattern {}: {}", locationPattern, e.getMessage());
          throw new RuntimeException(e);
        }
      }
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if((xstreamResource == null || !xstreamResource.exists()) &&
        (xstreamResourcePatterns == null || xstreamResourcePatterns.length == 0)) {
      log.error(
          "XStream resource not specified, no seeding will take place. Make sure the 'resource' or 'resourcePatterns' property are set.");
    } else {
      initializeXstream(xstream);
    }
  }

  public void setResourcePatterns(String... xstreamResourcePatterns) {
    this.xstreamResourcePatterns = xstreamResourcePatterns;
  }

  public void setResource(Resource xstreamResource) {
    this.xstreamResource = xstreamResource;
  }

  protected Resource getResource() {
    return xstreamResource;
  }

  public void setAliases(@Nullable Map<String, Class<?>> aliases) {
    this.aliases = aliases;
  }

  public void setDevelopmentSeed(boolean devSeed) {
    developmentSeed = devSeed;
  }

  @Override
  public void setResourceLoader(ResourceLoader resourceLoader) {
    resolver = (ResourcePatternResolver) resourceLoader;
  }

  protected boolean shouldSeed(WebApplication application) {
    if(developmentSeed) {
      // If this is a development seed, only seed if the WebApplication
      // was deployed
      // in development mode
      if(!WebApplication.DEVELOPMENT.equalsIgnoreCase(application.getConfigurationType())) {
        return false;
      }
    } else {
      // If this is not a development seed, only seed if the
      // WebApplication was deployed
      // in deployment mode
      if(!WebApplication.DEPLOYMENT.equalsIgnoreCase(application.getConfigurationType())) {
        return false;
      }
    }
    return true;
  }

  /**
   * Called after 'resource' has been processed.
   *
   * @param result
   */
  protected void handleXstreamResult(Object result) {

  }

  /**
   * Called after one resource from 'resourcePatterns' has been processed.
   *
   * @param resource
   * @param result
   */
  protected void handleXstreamResult(Resource resource, Object result) {

  }

  /**
   * Given a {@code Resource} this method passes the underlying {@code InputStream} to the configured {@code XStream}
   * instance.
   *
   * @param resource the {@code Resource} to load
   * @return the result of {@code XStream#fromXML(java.io.Reader)}
   */
  protected Object handleXtreamResource(Resource resource) {
    log.info("Loading resource {}.", resource);
    InputStreamReader reader = null;
    try {
      reader = new InputStreamReader(resource.getInputStream(), RESOURCE_ENCODING);
      return xstream.fromXML(new InputStreamReader(resource.getInputStream(), RESOURCE_ENCODING));
    } catch(UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    } catch(IOException e) {
      log.error("Error parsing XStream resource {}: {}", resource, e.getMessage());
      throw new RuntimeException(e);
    } catch(XStreamException e) {
      log.error("Invalid XStream resource {}: {}", resource, e.getMessage());
      throw e;
    } catch(RuntimeException e) {
      log.error("Error parsing XStream resource {}: {}", resource, e.getMessage());
      throw e;
    } finally {
      if(reader != null) {
        try {
          reader.close();
        } catch(Exception e) {
          // ignore
        }
      }
    }
  }

  protected void initializeXstream(XStream xstream) {
    xstream.setMode(XStream.ID_REFERENCES);
    if(aliases != null) {
      for(Map.Entry<String, Class<?>> aliasEntry : aliases.entrySet()) {
        log.debug("Adding XStream alias '{}' for class {}", aliasEntry.getKey(), aliasEntry.getValue());
        xstream.alias(aliasEntry.getKey(), aliasEntry.getValue());
      }
    }
  }

}
