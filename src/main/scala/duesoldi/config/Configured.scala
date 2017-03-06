package duesoldi.config

import duesoldi._

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
    jdbcDatabaseUrl = env.getOrElse("JDBC_DATABASE_URL", ""),
    jdbcDatabaseUsername = env.getOrElse("JDBC_DATABASE_USERNAME", ""),
    jdbcDatabasePassword = env.getOrElse("JDBC_DATABASE_PASSWORD", "")
  )
}
