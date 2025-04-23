/**
 * @file
 * @copyright Samuel Dussault ; GRIIS / Université de Sherbrooke
 * @licence https://www.forge.gouv.qc.ca/licence/liliq-r/
 * @version 1.0.0
 * @brief @~french Implémentation de la classe MMecApplication.
 * @brief @~english MMecApplication class implementation.
 */

package ca.griis.mmec;

import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Info;
import ca.griis.mmec.api.MMecFacadeService;
import ca.griis.mmec.api.MMecFacadeServiceBase;
import ca.griis.mmec.api.exception.ConnectionException;
import ca.griis.mmec.api.exception.DefaultOntopConfigurationNotFoundException;
import ca.griis.mmec.converter.LogLevelConverter;
import ca.griis.mmec.properties.ConnectionProperties;
import ca.griis.mmec.properties.FacadeProperties;
import ca.griis.mmec.properties.FacadeType;
import ca.griis.mmec.properties.MappingProperties;
import ca.griis.mmec.properties.MissingPropertyException;
import ca.griis.mmec.properties.builder.ConnectionPropertiesBuilder;
import ca.griis.mmec.properties.builder.FacadePropertiesBuilder;
import ca.griis.mmec.properties.builder.MappingPropertiesBuilder;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.exception.OntopReformulationException;
import it.unibz.inf.ontop.injection.impl.MMecConfiguration;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.concurrent.Callable;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * @brief @~english «Brief component description (class, interface, ...)»
 * @par Details
 *      «Detailed description of the component (optional)»
 * @par Model
 *      «Model (Abstract, automation, etc.) (optional)»
 * @par Conception
 *      «Conception description (criteria and constraints) (optional)»
 * @par Limits
 *      «Limits description (optional)»
 *
 * @brief @~french Application console pour la génération de façades mMec.
 * @par Détails
 *      S.O.
 * @par Modèle
 *      S.O.
 * @par Conception
 *      S.O.
 * @par Limites
 *      S.O.
 *
 * @par Historique
 *      2024-05-24 [SD] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
@Command(name = "mmec-cli", description = "Command line interface for the MMec application.",
    mixinStandardHelpOptions = true, version = "1.0")
public class MMecApplication implements Callable<Integer> {
  private static final GriisLogger logger = GriisLoggerFactory.getLogger(MMecApplication.class);

  private static final String driverName = "org.postgresql.Driver";

  @Option(names = {"-d", "--db-url"}, required = true,
      description = "Database JDBC URL to the source database. The database must be a PostgreSQL "
          + "database and contains the OntoRelCat.")
  private String jdbcUrl;

  @Option(names = {"-n", "--db-name"}, required = true, description = "Database name.")
  private String databaseName;

  @Option(names = {"-u", "--db-username"}, required = true, description = "Database username.")
  private String username;

  @Option(names = {"-p", "--db-password"}, required = true, description = "Database password.")
  private String password;

  @Option(names = {"-f", "--facade"}, type = FacadeType.class, defaultValue = "VIEWS",
      description = "Type of facade to generate. Options: ${COMPLETION-CANDIDATES}."
          + " Default: ${DEFAULT-VALUE}.")
  private FacadeType facadeType;

  @Option(names = {"-m", "--mapping-file"}, required = true,
      description = "Path to the mapping file.")
  private String mappingFile;

  @Option(names = {"-t", "--ontology-file"}, required = true,
      description = "Path to the ontology file.")
  private String ontologyFile;

  @Option(names = {"-i", "--id-ontorel"}, required = true,
      description = "OntoRel identifier in the OntoRelCat.")
  private String ontoRelId;

  @Option(names = {"-s", "--facade-schema"}, required = true,
      description = "Schema to use for the mapping.")
  private String mappingSchema;

  @Option(names = {"-l", "--log-level"}, type = Level.class, defaultValue = "ERROR",
      fallbackValue = "ERROR", converter = LogLevelConverter.class,
      description = "Set the log level. Options: TRACE, DEBUG, INFO, WARN, ERROR. "
          + "Default: ${DEFAULT-VALUE}.")
  private Level logLevel;

  @Option(names = {"-o", "--output"}, required = true, description = "Output file for the facade.")
  private String outputFilePath;

  private final MMecFacadeService mmecService;
  private final MMecConfiguration.MMecConfigurationBuilder configBuilder;

  public MMecApplication(MMecFacadeService mmecService,
      MMecConfiguration.MMecConfigurationBuilder configBuilder) {
    this.mmecService = mmecService;
    this.configBuilder = configBuilder;
  }

  /**
   * @brief @~english
   * @param args
   *
   * @brief @~french Démarre l'application console de gestion de commandes.
   * @param args - Arguments de la ligne de commande.
   *
   * @par Tâches
   *      S.O.
   */
  public static void main(String[] args) {
    logger.info(Info.APP_STARTING);

    MMecFacadeService mmecFacadeService = new MMecFacadeServiceBase();
    MMecConfiguration.MMecConfigurationBuilder configBuilder =
        new MMecConfiguration.MMecConfigurationBuilder();
    MMecApplication mmecApplication =
        new MMecApplication(mmecFacadeService, configBuilder);

    int exitCode = new CommandLine(mmecApplication)
        .setOptionsCaseInsensitive(true)
        .setSubcommandsCaseInsensitive(true)
        .execute(args);

    logger.info(Info.APP_CLOSING);
    logger.debug("Exit code: {}", exitCode);

    System.exit(exitCode);
  }

  @Override
  public Integer call() {
    logger.info("Creating facade...");
    logger.debug("Arguments: {}", this.toString());

    setLogLevel(logLevel);

    try {
      ConnectionProperties connectionProperties =
          new ConnectionPropertiesBuilder()
              .withDriverName(driverName)
              .withDatabaseName(databaseName)
              .withJdbcUrl(jdbcUrl)
              .withUsername(username)
              .withPassword(password)
              .build();

      logger.debug("Connection properties: {}", connectionProperties);

      MappingProperties mappingProperties =
          new MappingPropertiesBuilder()
              .withOntoRelId(ontoRelId)
              .withMappingSchema(mappingSchema)
              .withR2rmlMappingFilePath(mappingFile)
              .withOntologyFilePath(ontologyFile)
              .build();

      logger.debug("Mapping properties: {}", mappingProperties);

      FacadeProperties facadeProperties =
          new FacadePropertiesBuilder()
              .withFacadeType(facadeType)
              .build();

      logger.debug("Facade properties: {}", facadeProperties);

      MMecConfiguration configuration = configBuilder
          .properties(connectionProperties.getPropertiesForOntop())
          .r2rmlMappingFile(mappingFile)
          .ontologyFile(ontologyFile)
          .mappingProperties(mappingProperties)
          .facadeProperties(facadeProperties)
          .build();

      String facade = mmecService.createFacade(configuration);

      if (facade == null) {
        logger.error("mMec service was not able to return a façade.");
        return CommandLine.ExitCode.SOFTWARE;
      }

      logger.info("Writing facade to file: {}", outputFilePath);
      Files.writeString(Paths.get(outputFilePath), facade, StandardCharsets.UTF_8);
      logger.info("Facade created successfully.");

      return CommandLine.ExitCode.OK;
    } catch (Exception e) {
      logException(e);
      return CommandLine.ExitCode.SOFTWARE;
    }
  }

  private void setLogLevel(Level level) {
    LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
    Logger rootLogger = loggerContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
    rootLogger.setLevel(level);
  }

  @Override
  public String toString() {
    return String.format("""
        MMecApplication{
          jdbcUrl='%s',
          databaseName='%s',
          username='%s',
          password='%s',
          facadeType=%s,
          mappingFile='%s',
          ontologyFile='%s',
          ontoRelId='%s',
          mappingSchema='%s',
          outputFilePath='%s',
          logLevel='%s'
        }
        """.replace("\n", "%n"), jdbcUrl, databaseName, username, password, facadeType, mappingFile,
        ontologyFile, ontoRelId, mappingSchema, outputFilePath, logLevel);
  }

  private static final HashMap<Class<? extends Throwable>, String> exceptionMessages =
      new HashMap<>() {
        {
          put(DefaultOntopConfigurationNotFoundException.class,
              "mMec-library was not able to load the default Ontop configuration."
                  + "You must contact the developer to fix the problem.");
          put(OntopConnectionException.class,
              "An error occurred while connecting Ontop to the database.");
          put(OBDASpecificationException.class, "An error was detected in the mapping file.");
          put(OntopReformulationException.class,
              "An error occurred while generating a query for the mapping.");
          put(IOException.class, "An error occurred while writing the facade to a file.");
          put(MissingPropertyException.class, "Cannot build the properties.");
          put(ConnectionException.class, "An error occurred while connecting to the database.");
        }
      };

  /**
   * @brief @~english
   * @param e
   *
   * @brief @~french Affiche l'erreur à l'utilisateur et ajoute l'exception en debug
   * @param e l'exception à logger
   *
   * @par Tâches
   *      S.O.
   */
  public static void logException(Throwable e) {
    String message = exceptionMessages.getOrDefault(e.getClass(), "An unexpected error occurred.");
    logger.error(message);

    if (e.getMessage() != null && !e.getMessage().isEmpty()) {
      logger.error("[detail] {}", e.getMessage());
    }

    Throwable cause;
    while ((cause = e.getCause()) != null) {
      if (cause.getMessage() != null) {
        logger.error("[cause] {}", cause.getMessage());
      }
      e = cause;
    }

    logger.debug(message, e);
  }
}
