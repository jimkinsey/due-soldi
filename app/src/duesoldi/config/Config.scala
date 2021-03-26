package duesoldi.config

import duesoldi.config.Config.Credentials
import duesoldi.config.Config.Credentials.ParseFailure.Malformed
import hammerspace.storage.JDBCConnection

case class Config(
  host: String,
  port: Option[Int],
  adminCredentials: Credentials,
  accessRecordingEnabled: Boolean,
  jdbcConnectionDetails: JDBCConnection.ConnectionDetails,
  imageBaseUrl: String,
  loggingEnabled: Boolean,
  loggerName: String,
  secretKey: String,
  assetBucket: String,
  features: Map[String, Boolean],
  accessRecordArchiveThreshold: Option[Int] = None,
  templatePath: Option[String] = None,
  furnitureCacheDurationHours: Option[Int] = None,
  loadFurnitureFromLocalProject: Boolean = false
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