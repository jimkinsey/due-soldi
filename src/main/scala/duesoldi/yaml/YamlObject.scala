package duesoldi.yaml

import duesoldi.json.CirceJson
import io.circe.yaml.parser

import scala.util.Try

object YamlObject
{
  def parse(yamlString: String): Either[YamlObject.Failure, Map[String,Any]] = {
    for {
      firstTry <- Try(parser.parse(yamlString)).toEither.left.map(Failure.Unexpected)
      json <- firstTry.left.map(_ => Failure.Malformed)
      obj <- json.asObject.toRight({ Failure.NotAnObject })
      map = CirceJson.toMap(obj)
    } yield {
      map
    }
  }

  sealed trait Failure
  object Failure
  {
    case class Unexpected(cause: Throwable) extends Failure
    case object Malformed extends Failure
    case object NotAnObject extends Failure
  }
}
