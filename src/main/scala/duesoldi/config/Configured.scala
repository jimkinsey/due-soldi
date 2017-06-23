package duesoldi.config

import duesoldi._
import duesoldi.storage.JDBCConnection

import scala.concurrent.duration.Duration
import scala.util.Try

trait Configured {

  def env: Env

  lazy val config = Config(
    furnitureVersion = env.getOrElse("FURNITURE_VERSION", "LOCAL"),
    furniturePath = env.getOrElse("FURNITURE_PATH", "src/main/resources/furniture"),
    furnitureCacheDuration = env.get("FURNITURE_CACHE_DURATION").flatMap(s => Try(Duration(s)).toOption).getOrElse(Duration.Zero),
    adminCredentials = env.get("ADMIN_CREDENTIALS").map(Config.Credentials(_)),
    accessRecordingEnabled = env.get("ACCESS_RECORDING_ENABLED").map(_.toBoolean).getOrElse(false),
    jdbcConnectionDetails = JDBCConnection.ConnectionDetails(
      url = env.getOrElse("JDBC_DATABASE_URL", ""),
      username = env.getOrElse("JDBC_DATABASE_USERNAME", ""),
      password = env.getOrElse("JDBC_DATABASE_PASSWORD", "")
    ),
    imageBaseUrl = env.getOrElse("IMAGE_BASE_URL", "")
  )
}
