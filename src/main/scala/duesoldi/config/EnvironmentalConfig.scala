package duesoldi.config

import duesoldi._
import duesoldi.storage.JDBCConnection

import scala.concurrent.duration.Duration
import scala.util.Try

object EnvironmentalConfig {
  def apply(env: Env): Config = {
    Config(
      host = env.getOrElse("HOST", "0.0.0.0"),
      port = env.get("PORT").map(_.toInt).getOrElse(8080),
      furnitureVersion = env.getOrElse("FURNITURE_VERSION", "LOCAL"),
      furniturePath = env.getOrElse("FURNITURE_PATH", "src/main/resources/furniture"),
      furnitureCacheDuration = env.get("FURNITURE_CACHE_DURATION").flatMap(s => Try(Duration(s)).toOption).getOrElse(Duration.Zero),
      adminCredentials = env.get("ADMIN_CREDENTIALS").flatMap(Config.Credentials.parsed(_).toOption),
      accessRecordingEnabled = env.get("ACCESS_RECORDING_ENABLED").map(_.toBoolean).getOrElse(false),
      jdbcConnectionDetails = JDBCConnection.ConnectionDetails(
        url = env.getOrElse("JDBC_DATABASE_URL", ""),
        username = env.getOrElse("JDBC_DATABASE_USERNAME", ""),
        password = env.getOrElse("JDBC_DATABASE_PASSWORD", "")
      ),
      imageBaseUrl = env.getOrElse("IMAGE_BASE_URL", ""),
      loggingEnabled = env.get("LOGGING_ENABLED").map(_.toBoolean).getOrElse(true),
      secretKey = env.getOrElse("SECRET_KEY", "")
    )
  }

  def toEnv(config: Config): Env = {
    Map(
      "HOST" -> config.host,
      "PORT" -> config.port.toString,
      "FURNITURE_VERSION" -> config.furnitureVersion,
      "FURNITURE_PATH" -> config.furniturePath,
      "FURNITURE_CACHE_DURATION" -> s"${config.furnitureCacheDuration.length} ${config.furnitureCacheDuration.unit.name}",
      "ADMIN_CREDENTIALS" -> config.adminCredentials.map(creds => s"${creds.username}:${creds.password}").getOrElse(""),
      "ACCESS_RECORDING_ENABLED" -> config.accessRecordingEnabled.toString,
      "JDBC_DATABASE_URL" -> config.jdbcConnectionDetails.url,
      "JDBC_DATABASE_PASSWORD" -> config.jdbcConnectionDetails.password,
      "JDBC_DATABASE_USERNAME" -> config.jdbcConnectionDetails.username,
      "IMAGE_BASE_URL" -> config.imageBaseUrl,
      "LOGGING_ENABLED" -> config.loggingEnabled.toString,
      "SECRET_KEY" -> config.secretKey
    )
  }

  def nonSensitive(env: Env): Env = env.filterNot { case (name, _) => isSensitive(name) }

  def isSensitive(name: String): Boolean = sensitiveVars.contains(name)

  lazy val sensitiveVars: Set[String] = Set("ADMIN_CREDENTIALS", "JDBC_DATABASE_USERNAME", "JDBC_DATABASE_PASSWORD", "SECRET_KEY")
}
