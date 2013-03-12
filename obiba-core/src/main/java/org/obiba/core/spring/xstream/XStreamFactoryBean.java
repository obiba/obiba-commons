package org.obiba.core.spring.xstream;

import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;

/**
 * {@link FactoryBean} implementation for creating and configuring {@link XStream} instances.
 * <p/>
 * <p/>
 * By default, this factory will create instances capable of injecting spring beans in objects unmarshaled by the
 * {@code XStream} instance. This behaviour can be deactivated through the {@code #injecting} property.
 */
public class XStreamFactoryBean implements FactoryBean, ApplicationContextAware {

  private boolean injecting = true;

  private int autowireType = -1;

  private ApplicationContext applicationContext;

  private Map<String, Class<?>> aliasMap;

  private Set<Class<?>> annotatedClasses;

  private Set<Converter> converters;

  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  public void setInjecting(boolean injecting) {
    this.injecting = injecting;
  }

  public void setAutowireType(int autowireType) {
    this.autowireType = autowireType;
  }

  public void setAliasMap(Map<String, Class<?>> aliasMap) {
    this.aliasMap = aliasMap;
  }

  public void setAnnotatedClasses(Set<Class<?>> annotatedClasses) {
    this.annotatedClasses = annotatedClasses;
  }

  public void setConverters(Set<Converter> converters) {
    this.converters = converters;
  }

  public Object getObject() throws Exception {
    return doCreateXStream();
  }

  @SuppressWarnings("unchecked")
  public Class getObjectType() {
    return XStream.class;
  }

  public boolean isSingleton() {
    return false;
  }

  protected XStream doCreateXStream() {
    return doConfigureXStream(new XStream(doCreateReflectionProvider()));
  }

  protected ReflectionProvider doCreateReflectionProvider() {
    ReflectionProvider provider = new XStream().getReflectionProvider();
    if(injecting) {
      InjectingReflectionProviderWrapper injecting = new InjectingReflectionProviderWrapper(provider,
          applicationContext);
      if(autowireType > -1) {
        injecting.setAutowireType(autowireType);
      }
      provider = injecting;
    }
    return provider;
  }

  protected XStream doConfigureXStream(XStream xstream) {
    if(aliasMap != null && aliasMap.size() > 0) {
      for(Map.Entry<String, Class<?>> entry : aliasMap.entrySet()) {
        xstream.alias(entry.getKey(), entry.getValue());
      }
    }

    if(annotatedClasses != null && annotatedClasses.size() > 0) {
      for(Class<?> c : annotatedClasses) {
        xstream.processAnnotations(c);
      }
    }

    if(converters != null && converters.size() > 0) {
      for(Converter c : converters) {
        xstream.registerConverter(c);
      }
    }

    return xstream;
  }
}
