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
      loggingEnabled = env.get("LOGGING_ENABLED").map(_.toBoolean).getOrElse(true)
    )
  }
}
