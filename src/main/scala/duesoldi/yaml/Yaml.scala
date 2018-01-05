package duesoldi.yaml

object Yaml
{
  def obj(yamlString: String): Either[Failure, Map[String,Any]] = {
    val iterator = yamlString.lines
    val pairs = for {
      line <- iterator
      key = line.takeWhile(_ != ':')
      afterKey = line.drop(key.length + 1).dropWhile(_ == ' ').trim
      value <- afterKey match {
        case "|" if iterator.hasNext => Some(iterator.takeWhile(_.matches("""^\s+.+$""")).map(deindent).mkString("\n"))
        case str if str.nonEmpty => Some(str)
        case _ => None
      }
    } yield {
      key -> value
    }
    Right(pairs.toMap)
  }

  def deindent(yamlString: String): String = {
    val indent = yamlString.lines.toSeq.head.takeWhile(_.isWhitespace).length
    yamlString.lines.map(_.drop(indent)).mkString("\n")
  }

  def arr(yamlString: String): Either[Failure, Seq[Any]] = {
    Right(yamlString.split("""\s*-\s*\n""").toSeq.filter(_.nonEmpty).map(deindent).map(item => obj(item).right.get))
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

