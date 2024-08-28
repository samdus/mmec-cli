/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation des tests unitaires de la classe MMecApplicationTest.
 * @brief @~english MMecApplicationTest unit tests implementation.
 */

package ca.griis.mmec.test.unit;

import ca.griis.mmec.MMecApplication;
import ca.griis.mmec.api.MMecFacadeService;
import ca.griis.mmec.api.exception.ConnectionException;
import ca.griis.mmec.api.exception.DefaultOntopConfigurationNotFound;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.exception.OntopReformulationException;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import picocli.CommandLine;

public class MMecApplicationTest {
  @TempDir
  static Path tempDir;

  @Test
  public void testWithoutArgs() {
    MMecFacadeService mmecFacadeService = Mockito.mock(MMecFacadeService.class);
    MMecApplication mmecApplication = new MMecApplication(mmecFacadeService);
    CommandLine commandLine = new CommandLine(mmecApplication);

    int exitCode = commandLine.execute();
    Assertions.assertEquals(CommandLine.ExitCode.USAGE, exitCode);
  }

  @Test
  public void testToString()
    throws OBDASpecificationException, OntopConnectionException, OntopReformulationException,
    IOException, DefaultOntopConfigurationNotFound, ConnectionException {
    MMecFacadeService mmecFacadeService = Mockito.mock(MMecFacadeService.class);
    MMecApplication mmecApplication = new MMecApplication(mmecFacadeService);
    CommandLine commandLine = new CommandLine(mmecApplication);

    String outputFile = tempDir.resolve("output.sql").toString();
    String expected = String.format("""
        MMecApplication{
          jdbcUrl='jdbc:postgresql://localhost:5432/source',
          databaseName='dbName',
          username='user',
          password='password',
          facadeType=VIEWS,
          mappingFile='mappingFile',
          ontologyFile='ontologyFile',
          ontoRelId='idOntorel',
          mappingSchema='mappingSchema',
          outputFilePath='%s',
          logLevel='ERROR'
        }
        """, outputFile);

    Mockito.when(mmecFacadeService.createFacade(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn("facade");

    int exitCode = commandLine.execute(
        "--db-url", "jdbc:postgresql://localhost:5432/source",
        "--db-name", "dbName",
        "--db-username", "user",
        "--db-password", "password",
        "--facade", "VIEWS",
        "--mapping-file", "mappingFile",
        "--ontology-file", "ontologyFile",
        "--id-ontorel", "idOntorel",
        "--facade-schema", "mappingSchema",
        "--output", outputFile);

    String actual = mmecApplication.toString();

    Assertions.assertEquals(CommandLine.ExitCode.OK, exitCode);
    Assertions.assertEquals(expected, actual);
  }
}
