package org.obiba.db4o;

import org.springframework.beans.factory.InitializingBean;

import com.db4o.ObjectContainer;

public class Db4oAccessor implements InitializingBean {

  private ObjectContainer objectContainer;
  
  public ObjectContainer getObjectContainer() {
    return objectContainer;
  }
  
  public void setObjectContainer(ObjectContainer objectContainer) {
    this.objectContainer = objectContainer;
  }

  public void afterPropertiesSet() throws Exception {
    if(objectContainer == null) {
      throw new IllegalStateException("missing objectContainer");
    }
  }
  
}
