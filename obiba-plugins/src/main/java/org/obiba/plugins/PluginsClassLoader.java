package org.obiba.plugins;

import java.net.URL;
import java.net.URLClassLoader;

public class PluginsClassLoader extends URLClassLoader {

  public PluginsClassLoader() {
    this(new URL[]{}, ClassLoader.getSystemClassLoader());
  }

  public PluginsClassLoader(URL[] urls, ClassLoader parent) {
    super(new URL[]{}, ClassLoader.getSystemClassLoader());
  }

  public void addURLToClassPath(URL url) {
    super.addURL(url);
  }
}
