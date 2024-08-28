/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe LogLevelConverter.
 * @brief @~english LogLevelConverter class implementation.
 */

package ca.griis.mmec.converter;

import ch.qos.logback.classic.Level;
import picocli.CommandLine;

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
 * @brief @~french Classe permettant de convertir un paramètre en chaîne de caractère en
 *        Level de LogBack.
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
 *      2024-08-20 [SD] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public class LogLevelConverter implements CommandLine.ITypeConverter<Level> {

  @Override
  public Level convert(String value) {
    return switch (value) {
      case "TRACE" -> Level.TRACE;
      case "DEBUG" -> Level.DEBUG;
      case "INFO" -> Level.INFO;
      case "WARN" -> Level.WARN;
      case "ERROR" -> Level.ERROR;
      default -> throw new IllegalArgumentException("Invalid log level: " + value);
    };
  }
}
