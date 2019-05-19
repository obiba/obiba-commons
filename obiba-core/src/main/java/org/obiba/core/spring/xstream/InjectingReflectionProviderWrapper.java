/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.core.spring.xstream;

import javax.annotation.Nullable;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;

import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.converters.reflection.ReflectionProviderWrapper;

/**
 *
 */
public class InjectingReflectionProviderWrapper extends ReflectionProviderWrapper {

  private final ApplicationContext applicationContext;

  private int autowireType = AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE;

  public InjectingReflectionProviderWrapper(ReflectionProvider wrapper, ApplicationContext applicationContext) {
    super(wrapper);
    this.applicationContext = applicationContext;
  }

  public void setAutowireType(int autowireType) {
    this.autowireType = autowireType;
  }

  @Nullable
  @SuppressWarnings("rawtypes")
  @Override
  public Object newInstance(Class type) {
    // Let the wrapped instance create the bean
    Object value = super.newInstance(type);
    if(value != null) {
      // If we can, autowire the instance
      if(applicationContext != null && applicationContext.getAutowireCapableBeanFactory() != null) {
        // Autowire by type
        applicationContext.getAutowireCapableBeanFactory().autowireBeanProperties(value, autowireType, false);
      }
    }
    return value;
  }

}
