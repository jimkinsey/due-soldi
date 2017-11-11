package duesoldi.config

import duesoldi._
import duesoldi.dependencies.Features
import duesoldi.storage.JDBCConnection

object EnvironmentalConfig
{
  def apply(env: Env): Config = {
    Config(
      host = env.getOrElse("HOST", "0.0.0.0"),
      port = env.get("PORT").map(_.toInt).getOrElse(8080),
      furniturePath = env.getOrElse("FURNITURE_PATH", "src/main/resources/furniture"),
      adminCredentials = Config.Credentials.parsed(env("ADMIN_CREDENTIALS")).right.get,
      accessRecordingEnabled = env.get("ACCESS_RECORDING_ENABLED").map(_.toBoolean).getOrElse(false),
      jdbcConnectionDetails = JDBCConnection.ConnectionDetails(
        url = env.getOrElse("JDBC_DATABASE_URL", ""),
        username = env.getOrElse("JDBC_DATABASE_USERNAME", ""),
        password = env.getOrElse("JDBC_DATABASE_PASSWORD", "")
      ),
      imageBaseUrl = env.getOrElse("IMAGE_BASE_URL", ""),
      loggingEnabled = env.get("LOGGING_ENABLED").map(_.toBoolean).getOrElse(true),
      loggerName = env.getOrElse("LOGGER_NAME", ""),
      secretKey = env.getOrElse("SECRET_KEY", ""),
      features = Features.featureStatuses(env)
    )
  }

  def toEnv(config: Config): Env = {
    Map(
      "HOST" -> config.host,
      "PORT" -> config.port.toString,
      "FURNITURE_PATH" -> config.furniturePath,
      "ADMIN_CREDENTIALS" -> s"${config.adminCredentials.username}:${config.adminCredentials.password}",
      "ACCESS_RECORDING_ENABLED" -> config.accessRecordingEnabled.toString,
      "JDBC_DATABASE_URL" -> config.jdbcConnectionDetails.url,
      "JDBC_DATABASE_PASSWORD" -> config.jdbcConnectionDetails.password,
      "JDBC_DATABASE_USERNAME" -> config.jdbcConnectionDetails.username,
      "IMAGE_BASE_URL" -> config.imageBaseUrl,
      "LOGGING_ENABLED" -> config.loggingEnabled.toString,
      "LOGGER_NAME" -> config.loggerName,
      "SECRET_KEY" -> config.secretKey
    ) ++ config.features.map {
      case (key, value) => s"FEATURE_$key" -> (if (value) "on" else "off")
    }
  }

  def nonSensitive(env: Env): Env = env.filterNot { case (name, _) => isSensitive(name) }

  def isSensitive(name: String): Boolean = sensitiveVars.contains(name)

  lazy val sensitiveVars: Set[String] = Set("ADMIN_CREDENTIALS", "JDBC_DATABASE_USERNAME", "JDBC_DATABASE_PASSWORD", "SECRET_KEY")
}
