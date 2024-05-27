/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe MMecApplication.
 * @brief @~english MMecApplication class implementation.
 */

package ca.griis.mmec;

import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Info;
import ca.griis.logger.statuscode.Trace;
import ca.griis.mmec.properties.ConnectionProperties;
import ca.griis.mmec.properties.FacadeProperties;
import ca.griis.mmec.properties.FacadeType;
import ca.griis.mmec.properties.MappingProperties;
import ca.griis.mmec.properties.builder.ConnectionPropertiesBuilder;
import ca.griis.mmec.properties.builder.FacadePropertiesBuilder;
import ca.griis.mmec.properties.builder.MappingPropertiesBuilder;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

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
 * @brief @~french «Brève description de la composante (classe, interface, ...)»
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
@Command(
  name = "mmec-cli",
  description = "Command line interface for the MMec application.",
  mixinStandardHelpOptions = true,
  version = "1.0"
)
public class MMecApplication implements Runnable {
  private static final GriisLogger logger = GriisLoggerFactory.getLogger(MMecApplication.class);

  private static final String driverName = "org.postgresql.Driver";

  @Option(names = {"-d", "--dburl"},
    required = true,
    description = "Database JDBC URL to the source database. The database must be a PostgreSQL database " +
      "and contains the OntoRelCat.")
  private String jdbcUrl;

  @Option(names = {"-u", "--dbusername"},
    required = true,
    description = "Database username.")
  private String username;

  @Option(names = {"-p", "--dbpassword"},
    required = true,
    description = "Database password.")
  private String password;

  @Option(names = {"-f", "--facade"},
    type = FacadeType.class,
    defaultValue = "VIEWS",
    description = "Type of facade to generate. Options: ${COMPLETION-CANDIDATES}. Default: ${DEFAULT-VALUE}.")
  private FacadeType facadeType;

  @Option(names = {"-m", "--mapping"},
    required = true,
    description = "Path to the mapping file.")
  private String mappingFile;

  @Option(names = {"-o", "--ontologyFile"},
    required = true,
    description = "Path to the ontology file.")
  private String ontologyFile;

  @Option(names = {"-i", "--id-ontorel"},
    required = true,
    description = "OntoRel identifier in the OntoRelCat.")
  private String ontoRelId;

  @Option(names = {"-s", "--schema"},
    required = true,
    description = "Schema to use for the mapping.")
  private String mappingSchema;


  @Override
  public void run() {
    logger.info("Creating facade with parameters :");

    logger.info("Driver name: {}", driverName);
    logger.info("\tJDBC URL: {}", jdbcUrl);
    logger.info("\tUsername: {}", username);
    logger.info("\tPassword (hidden)");
    logger.info("\tFacade type: {}", facadeType);
    logger.info("\tMapping file: {}", mappingFile);
    logger.info("\tOntology file: {}", ontologyFile);
    logger.info("\tOntoRel ID: {}", ontoRelId);
    logger.info("\tMapping schema: {}", mappingSchema);

    ConnectionProperties connectionProperties = new ConnectionPropertiesBuilder()
      .withDriverName(driverName)
      .withJdbcUrl(jdbcUrl)
      .withUsername(username)
      .withPassword(password)
      .build();

    MappingProperties mappingProperties = new MappingPropertiesBuilder()
      .withOntoRelId(ontoRelId)
      .withMappingSchema(mappingSchema)
      .withR2rmlMappingFilePath(mappingFile)
      .withOntologyFilePath(ontologyFile)
      .build();

    FacadeProperties facadeProperties = new FacadePropertiesBuilder()
      .withFacadeType(facadeType)
      .build();

    String facade = new MMecFacadeServiceBase()
      .createFacade(connectionProperties, mappingProperties, facadeProperties);

    System.out.println(facade);
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

    int exitCode = new CommandLine(new MMecApplication())
      .setOptionsCaseInsensitive(true)
      .setSubcommandsCaseInsensitive(true)
      .execute(args);

    logger.info(Info.APP_CLOSING, exitCode);
    System.exit(exitCode);
  }
}
