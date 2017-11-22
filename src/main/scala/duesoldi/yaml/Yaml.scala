package duesoldi.yaml

import duesoldi.json.CirceJson
import io.circe.yaml.parser

import scala.util.Try

object Yaml
{
  def obj(yamlString: String): Either[Yaml.Failure, Map[String,Any]] = {
    for {
      firstTry <- Try(parser.parse(yamlString)).toEither.left.map(Failure.Unexpected)
      json <- firstTry.left.map(_ => Failure.Malformed)
      obj <- json.asObject.toRight({ Failure.NotAnObject })
      map = CirceJson.toMap(obj)
    } yield {
      map
    }
  }

  def arr(yamlString: String): Either[Failure, Seq[Any]] = {
    for {
      firstTry <- Try(parser.parse(yamlString)).toEither.left.map(Failure.Unexpected)
      json <- firstTry.left.map(_ => Failure.Malformed)
      arr <- json.asArray.toRight({ Failure.NotAnArray })
      seq = CirceJson.toSeq(arr)
    } yield {
      seq
    }
  }

  sealed trait Failure
  object Failure
  {
    case class Unexpected(cause: Throwable) extends Failure
    case object Malformed extends Failure
    case object NotAnObject extends Failure
    case object NotAnArray extends Failure
  }
}

