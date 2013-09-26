package org.obiba.wicket.util.seed;

import java.util.List;

import org.apache.wicket.protocol.http.WebApplication;
import org.obiba.wicket.application.WebApplicationStartupListener;

/**
 * Implements {@link WebApplicationStartupListener} to allow seeding a database with a list of {@link DatabaseSeed}
 * instances.
 */
public class DatabaseSeedWebApplicationStartupListener implements WebApplicationStartupListener {

  private List<DatabaseSeed> databaseSeeds;

  @Override
  public void shutdown(WebApplication application) {
    // Nothing to do.
  }

  @Override
  public void startup(WebApplication application) {
    if(databaseSeeds != null) {
      for(DatabaseSeed seed : databaseSeeds) {
        seed.seedDatabase(application);
      }
    }
  }

  public void setDatabaseSeeds(List<DatabaseSeed> databaseSeeds) {
    this.databaseSeeds = databaseSeeds;
  }

  public List<DatabaseSeed> getDatabaseSeeds() {
    return databaseSeeds;
  }

}
