/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.core.service.impl.hibernate.ddl;

import java.util.Properties;

import javax.annotation.Nonnull;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.ImprovedNamingStrategy;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("UnusedDeclaration")
public class DDLGenerator {

  static private final Logger log = LoggerFactory.getLogger(DDLGenerator.class);

  //  System constants for the current platform directory token
  static final String FILE_SEP = System.getProperty("file.separator");

  public static final String DEFAULT_DIALECT = "org.hibernate.dialect.MySQLInnoDBDialect";

  private SessionFactory sessionFactory;

  public void setSessionFactory(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  /**
   * Generate DDL scripts in default dialect.
   */
  public void generate() {
    generate(null, null, getConfiguration(DEFAULT_DIALECT));
  }

  /**
   * Generate DDL script in given dialect.
   *
   * @param path where to write file
   * @param suffix DDL script file suffix
   * @param dialect
   */
  public void generate(String path, String suffix, String dialect) {
    generate(path, suffix, getConfiguration(dialect));
  }

  private Configuration getConfiguration(String dialect) {
    log.warn("Creating Hibernate Configuration from Session...");

    Configuration annoteConf = new Configuration();
    for(Object metaKey : sessionFactory.getAllClassMetadata().keySet()) {
      log.info("annotatedClass={}", metaKey);
      try {
        annoteConf.addAnnotatedClass(Class.forName((String) metaKey));
      } catch(Exception e) {
        log.error("Failed adding annotated class.", e);
        return null;
      }
    }

    annoteConf.setNamingStrategy(ImprovedNamingStrategy.INSTANCE);
    Properties props = new Properties();
    props.put("hibernate.dialect", DEFAULT_DIALECT);
    annoteConf.addProperties(props);

    return annoteConf;
  }

  /**
   * Generate DDL script with given configuration.
   *
   * @param path where to write file
   * @param suffix DDL script file suffix
   * @param conf
   */
  private void generate(String path, String suffix, @Nonnull Configuration conf) {
    //noinspection ConstantConditions
    if(conf == null) throw new IllegalArgumentException("Configuration cannot be null.");

    SchemaExport schemaExport = new SchemaExport(conf);
    schemaExport.setDelimiter(";");

    String safePath = path == null ? "" : path;

    if(safePath.length() != 0 && !path.endsWith(FILE_SEP)) safePath += FILE_SEP;

    String safeSuffix = suffix;
    if(suffix == null || suffix.isEmpty()) {
      String dialect = conf.getProperty("hibernate.dialect");
      safeSuffix = dialect == null ? ".sql" : "_" + dialect.substring(dialect.lastIndexOf('.') + 1) + ".sql";
    }

    schemaExport.setHaltOnError(true);

    // Despite the name, the generated create
    // scripts WILL include drop statements at
    // the top of the script!
    log.info("CREATE DDL...");
    schemaExport.setOutputFile(safePath + "create" + safeSuffix);
    schemaExport.create(false, false);

    // Generates DROP statements only
    log.info("DROP DDL...");
    schemaExport.setOutputFile(safePath + "drop" + safeSuffix);
    schemaExport.drop(false, false);

    log.error("exceptions: {}", schemaExport.getExceptions().toString());
  }

}
