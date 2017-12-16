package sommelier.routing

import scala.util.matching.Regex

object PathParams
{
  sealed trait Failure
  object Failure
  {
    case object PathMatchFailure extends Failure
  }

  def apply(pattern: String)(path: String): Either[Failure, Map[String,String]] = {
    val patternSegments = segments(pattern)
    def firstSegment(path: String): Option[String] = path.split('/').headOption.filter(_.nonEmpty)
    def remainingAfterFirstSegment(path: String): String = path.split('/').tail.mkString("/")
    def hasVars(patternSegments: Seq[String]): Boolean = patternSegments.exists(_.startsWith(":")) || patternSegments.contains("*")

    if (!hasVars(patternSegments)) {
      if (pattern != path) {
        return Left(Failure.PathMatchFailure)
      }
      else {
        return Right(Map.empty)
      }
    }

    val (result, _) = patternSegments.foldLeft[(Either[Failure, Map[String,String]], String)]((Right(Map.empty), path.dropWhile(_ == '/'))) {
      case ((Right(acc), remaining), segment) if segment.startsWith(":") && firstSegment(remaining).isDefined =>
        (Right(acc ++ Map(segment.drop(1) -> firstSegment(remaining).get)), remainingAfterFirstSegment(remaining))
      case ((Right(acc), remaining), "*") if remaining.dropWhile(_ == '/').nonEmpty =>
        (Right(acc ++ Map("*" -> remaining)), "")
      case ((acc @ Right(_), remaining), segment) if firstSegment(remaining).contains(segment) =>
        (acc, remainingAfterFirstSegment(remaining))
      case (success @ (Right(_), ""), "") =>
        success
      case (fail @ (Left(_), _), _) =>
        fail
      case ((Right(_), remaining), segment) =>
        (Left(Failure.PathMatchFailure), path)
    }

    result
  }

  def segments(path: String): Seq[String] =
    path
      .split('/').toSeq
      .filter(_.nonEmpty)

  lazy val PathVariable: Regex = """^:(.+)$""".r
}
