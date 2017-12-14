package duesoldi.config

import duesoldi.config.Config.Credentials
import duesoldi.config.Config.Credentials.ParseFailure.Malformed
import duesoldi.storage.JDBCConnection

import scala.concurrent.duration.Duration

case class Config(
  host: String,
  port: Int,
  furniturePath: String,
  adminCredentials: Credentials,
  accessRecordingEnabled: Boolean,
  jdbcConnectionDetails: JDBCConnection.ConnectionDetails,
  imageBaseUrl: String,
  loggingEnabled: Boolean,
  loggerName: String,
  secretKey: String,
  features: Map[String, Boolean]
)

object Config
{
  def parse(str: String): Map[String, String] = {
    str.split(' ').map { case EnvVar(name, value) => name -> value } toMap
  }

  lazy val EnvVar = """([A-Z_]+)=(.*)""".r

  case class Credentials(username: String, password: String)

  object Credentials
  {
    sealed trait ParseFailure
    object ParseFailure
    {
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