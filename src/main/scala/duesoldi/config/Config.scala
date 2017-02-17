package duesoldi.config

import duesoldi.config.Config.Credentials

import scala.concurrent.duration.Duration

case class Config(
  blogStorePath: String,
  furnitureVersion: String,
  furniturePath: String,
  furnitureCacheDuration: Duration,
  adminCredentials: Option[Credentials]
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