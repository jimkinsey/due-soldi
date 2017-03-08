package duesoldi.config

import duesoldi.config.Config.Credentials
import duesoldi.storage.JDBCConnection

import scala.concurrent.duration.Duration

case class Config(
  furnitureVersion: String,
  furniturePath: String,
  furnitureCacheDuration: Duration,
  adminCredentials: Option[Credentials],
  accessRecordingEnabled: Boolean,
  jdbcConnectionDetails: JDBCConnection.ConnectionDetails
)

object Config {
  case class Credentials(username: String, password: String)

  object Credentials {
    def apply(colonSeparated: String): Credentials = {
      colonSeparated.split(':').toList match {
        case username :: password :: _ => Credentials(username, password)
        case oth => println(s"Got $oth"); Credentials("", "") // FIXME
       }
    }
  }
}