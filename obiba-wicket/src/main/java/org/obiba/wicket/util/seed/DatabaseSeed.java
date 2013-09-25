package org.obiba.wicket.util.seed;

import org.apache.wicket.protocol.http.WebApplication;

public interface DatabaseSeed {

  void seedDatabase(WebApplication application);

}
