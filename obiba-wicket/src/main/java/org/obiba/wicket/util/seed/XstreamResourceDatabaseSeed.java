package org.obiba.wicket.util.seed;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.wicket.protocol.http.WebApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Transactional;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;

public class XstreamResourceDatabaseSeed implements DatabaseSeed, InitializingBean {

  private final Logger log = LoggerFactory.getLogger(XstreamResourceDatabaseSeed.class);

  private XStream xstream = new XStream();

  private Resource xstreamResource;

  private Map<String, Class<?>> aliases;

  private String resourceEncoding = "ISO-8859-1";

  private boolean developmentSeed = false;

  @SuppressWarnings("unchecked")
  @Transactional
  public void seedDatabase(WebApplication application) {
    if(shouldSeed(application) == false) {
      return;
    }
    if (xstreamResource != null) {
      Object xstreamResult = null;
      try {
        xstreamResult = xstream.fromXML(xstreamResource.getInputStream(), resourceEncoding);
      } catch (UnsupportedEncodingException e) {
        throw new RuntimeException(e);
      } catch (IOException e) {
        log.error("Error parsing XStream Resource.", e);
        throw new RuntimeException(e);
      } catch (XStreamException e) {
        log.error("Invalid XStream Resource.", e);
        throw e;
      }

      try {
        handleXstreamResult(xstreamResult);
      } catch (RuntimeException e) {
        log.error("Error parsing XStream Resource.", e);
        throw e;
      }
    }
  }

  public void afterPropertiesSet() throws Exception {
    if (this.xstreamResource == null || this.xstreamResource.exists() == false) {
      log
          .error("XStream resource not specified, no seeding will take place. Make sure the 'resource' property is set.");
    } else {
      initializeXstream(xstream);
    }
  }

  public void setResource(Resource xstreamResource) {
    this.xstreamResource = xstreamResource;
  }

  public void setAliases(Map<String, Class<?>> aliases) {
    this.aliases = aliases;
  }

  public void setDevelopmentSeed(boolean devSeed) {
    this.developmentSeed = devSeed;
  }

  protected boolean shouldSeed(WebApplication application) {
    if (developmentSeed == true) { 
      // If this is a development seed, only seed if the WebApplication was deployed
      // in development mode
      if(WebApplication.DEVELOPMENT.equalsIgnoreCase(application.getConfigurationType()) == false) {
        return false;
      }
    } else {
      // If this is not a development seed, only seed if the WebApplication was deployed
      // in deployment mode
      if(WebApplication.DEPLOYMENT.equalsIgnoreCase(application.getConfigurationType()) == false) {
        return false;
      }
    }
    return true;
  }

  protected void handleXstreamResult(Object result) {

  }

  protected void initializeXstream(XStream xstream) {
    xstream.setMode(XStream.ID_REFERENCES);
    if (this.aliases != null) {
      for (Map.Entry<String, Class<?>> aliasEntry : aliases.entrySet()) {
        log.debug("Adding XStream alias '{}' for class {}", aliasEntry.getKey(), aliasEntry.getValue());
        xstream.alias(aliasEntry.getKey(), aliasEntry.getValue());
      }
    }
  }
}
