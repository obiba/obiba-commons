package org.obiba.runtime.jdbc;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;

import javax.sql.DataSource;

import org.obiba.core.util.StreamUtil;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;

import com.thoughtworks.xstream.XStream;

public class DatabaseProductRegistry {

  private final Collection<DatabaseProduct> databaseProducts = new HashSet<>();

  @SuppressWarnings("unchecked")
  public DatabaseProductRegistry() {
    InputStream is = getClass().getResourceAsStream("database-products.xml");
    if(is == null) {
      throw new IllegalStateException(
          "database-products.xml file not found. It should be packaged with the obiba-core jar.");
    }
    try {
      databaseProducts.addAll((Collection<? extends DatabaseProduct>) new XStream().fromXML(is, "UTF-8"));
    } finally {
      StreamUtil.silentSafeClose(is);
    }
  }

  public DatabaseProduct getDatabaseProduct(DataSource dataSource) {
    try {
      String dbProductName = (String) JdbcUtils.extractDatabaseMetaData(dataSource, "getDatabaseProductName");
      return getDatabaseProduct(dbProductName);
    } catch(MetaDataAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public DatabaseProduct getDatabaseProduct(String dbProductName) {
    if(dbProductName == null) {
      throw new NullPointerException("dbProductName cannot be null");
    }
    for(DatabaseProduct dp : databaseProducts) {
      if(dp.isForProductName(dbProductName)) {
        return dp;
      }
    }
    throw new IllegalStateException("Unknown database product " + dbProductName);
  }

}
