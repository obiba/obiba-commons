package org.obiba.shiro;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 * Utility method library mimicking the Shiro one but allowing to return an <code>URL</code>.
 */
public class ClassUtils {
  private static final Logger log = LoggerFactory.getLogger(ClassUtils.class);

  private static final ClassLoaderAccessor THREAD_CL_ACCESSOR = new ExceptionIgnoringAccessor() {
    @Override
    protected ClassLoader doGetClassLoader() {
      return Thread.currentThread().getContextClassLoader();
    }
  };

  private static final ClassLoaderAccessor CLASS_CL_ACCESSOR = new ExceptionIgnoringAccessor() {
    @Override
    protected ClassLoader doGetClassLoader() {
      return ClassUtils.class.getClassLoader();
    }
  };

  private static final ClassLoaderAccessor SYSTEM_CL_ACCESSOR = new ExceptionIgnoringAccessor() {
    @Override
    protected ClassLoader doGetClassLoader() {
      return ClassLoader.getSystemClassLoader();
    }
  };

  /**
   * Returns the specified resource URL by checking the current thread's
   * {@link Thread#getContextClassLoader() context class loader}, then the
   * current ClassLoader ({@code ClassUtils.class.getClassLoader()}), then the system/application
   * ClassLoader ({@code ClassLoader.getSystemClassLoader()}, in that order, using
   * {@link ClassLoader#getResourceAsStream(String) getResourceAsStream(name)}.
   *
   * @param name the name of the resource to acquire from the classloader(s).
   * @return the URL of the resource found, or {@code null} if the resource cannot be found from any
   * of the three mentioned ClassLoaders.
   */
  public static URL getResource(String name) {

    URL is = THREAD_CL_ACCESSOR.getResource(name);

    if (is == null) {
      if (log.isTraceEnabled()) {
        log.trace("Resource [" + name + "] was not found via the thread context ClassLoader.  Trying the " +
            "current ClassLoader...");
      }
      is = CLASS_CL_ACCESSOR.getResource(name);
    }

    if (is == null) {
      if (log.isTraceEnabled()) {
        log.trace("Resource [" + name + "] was not found via the current class loader.  Trying the " +
            "system/application ClassLoader...");
      }
      is = SYSTEM_CL_ACCESSOR.getResource(name);
    }

    if (is == null && log.isTraceEnabled()) {
      log.trace("Resource [" + name + "] was not found via the thread context, current, or " +
          "system/application ClassLoaders.  All heuristics have been exhausted.  Returning null.");
    }

    return is;
  }

  private interface ClassLoaderAccessor {
    URL getResource(String name);
  }

  private static abstract class ExceptionIgnoringAccessor implements ClassLoaderAccessor {

    public URL getResource(String name) {
      ClassLoader cl = getClassLoader();
      if (cl != null) {
        return cl.getResource(name);
      }
      return null;
    }

    protected final ClassLoader getClassLoader() {
      try {
        return doGetClassLoader();
      } catch (Throwable t) {
        if (log.isDebugEnabled()) {
          log.debug("Unable to acquire ClassLoader.", t);
        }
      }
      return null;
    }

    protected abstract ClassLoader doGetClassLoader();
  }
}
