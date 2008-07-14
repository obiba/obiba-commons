package org.obiba.wicket.application;

import org.apache.wicket.Application;
import org.apache.wicket.protocol.http.WebApplication;

/**
 * Listener for startup/shutdown events on the Wicket Application. This is useful for executing arbitrary code during
 * application startup.
 * <p>
 * The startup method is called inside the application's {@link Application#init} method. As such, it blocks the
 * application listening for incoming requests. If the code to be executed is potentially long-running, consider using
 * an event mechanism instead of this listener interface.
 * <p>
 * The shutdown hook is provided to cleanup anything before the application actually shuts down.
 * 
 */
public interface WebApplicationStartupListener {

  void startup(WebApplication application);

  void shutdown(WebApplication application);

}
