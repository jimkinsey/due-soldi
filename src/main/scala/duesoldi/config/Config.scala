package duesoldi.config

import duesoldi.config.Config.Credentials
import duesoldi.config.Config.Credentials.ParseFailure.Malformed
import duesoldi.storage.JDBCConnection

import scala.concurrent.duration.Duration

case class Config(
  host: String,
  port: Int,
  furnitureVersion: String,
  furniturePath: String,
  furnitureCacheDuration: Duration,
  adminCredentials: Option[Credentials],
  accessRecordingEnabled: Boolean,
  jdbcConnectionDetails: JDBCConnection.ConnectionDetails,
  imageBaseUrl: String,
  loggingEnabled: Boolean,
  secretKey: String 
)

object Config {
  case class Credentials(username: String, password: String)

  object Credentials {
    sealed trait ParseFailure
    object ParseFailure {
      case object Malformed extends ParseFailure
    }
    def parsed(colonSeparated: String): Either[ParseFailure, Credentials] = {
      colonSeparated.split(':').toList match {
        case username :: password :: _ => Right(Credentials(username, password))
        case _ => Left(Malformed)
       }
    }
  }
}