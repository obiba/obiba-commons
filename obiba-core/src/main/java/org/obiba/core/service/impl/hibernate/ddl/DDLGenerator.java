package org.obiba.core.service.impl.hibernate.ddl;

import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
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

    AnnotationConfiguration annoteConf = new AnnotationConfiguration();
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
  @SuppressWarnings({ "AssignmentToMethodParameter", "PMD.AvoidReassigningParameters", "PMD.NcssMethodCount" })
  private void generate(String path, String suffix, Configuration conf) {
    if(conf == null) throw new IllegalArgumentException("Configuration cannot be null.");

    SchemaExport schemaExport = new SchemaExport(conf, conf.buildSettings());
    schemaExport.setDelimiter(";");

    if(path == null) path = "";

    if(path.length() != 0 && !path.endsWith(FILE_SEP)) path += FILE_SEP;

    if(suffix == null || suffix.isEmpty()) {
      String dialect = conf.getProperty("hibernate.dialect");
      suffix = dialect == null ? ".sql" : "_" + dialect.substring(dialect.lastIndexOf('.') + 1) + ".sql";
    }

    schemaExport.setHaltOnError(true);

    // Despite the name, the generated create
    // scripts WILL include drop statements at
    // the top of the script!
    log.info("CREATE DDL...");
    schemaExport.setOutputFile(path + "create" + suffix);
    schemaExport.create(false, false);

    // Generates DROP statements only
    log.info("DROP DDL...");
    schemaExport.setOutputFile(path + "drop" + suffix);
    schemaExport.drop(false, false);

    log.error("exceptions: {}", schemaExport.getExceptions().toString());
  }

}
