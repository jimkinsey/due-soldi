package sommelier.routing

object PathParams
{
  sealed trait Failure
  object Failure
  {
    case object PathMatchFailure extends Failure
  }

  def apply(pattern: String)(path: String): Either[Failure, Map[String,String]] = {
    if (!isVariable(pattern)) {
      if (pattern != path) {
        return Left(Failure.PathMatchFailure)
      }
      else {
        return Right(Map.empty)
      }
    }

    val (result, _) = segments(pattern).foldLeft[(Either[Failure, Map[String,String]], String)]((Right(Map.empty), path.dropWhile(_ == '/'))) {
      case ((acc @ Right(_), Remainder(varValue, tail)), Var(name)) =>
        (acc.map(_ ++ Map(name -> varValue)), tail)
      case ((acc @ Right(_), WildcardValue(value)), Wildcard(_)) =>
        (acc.map(_ ++ Map("*" -> value)), "")
      case ((acc @ Right(_), Remainder(value, tail)), literal) if literal == value =>
        (acc, tail)
      case (success @ (Right(_), ""), "") =>
        success
      case (fail @ (Left(_), _), _) =>
        fail
      case ((Right(_), remaining), segment) =>
        (Left(Failure.PathMatchFailure), path)
    }

    result
  }

  def isVariable(pattern: String): Boolean =
    segments(pattern).exists { segment =>
      segment.startsWith(":") || segment == "*"
    }

  def segments(path: String): Seq[String] =
    path
      .split('/').toSeq
      .filter(_.nonEmpty)

  object Var
  {
    def unapply(segment: String): Option[String] = {
      if (segment.startsWith(":")) Some(segment.drop(1)) else None
    }
  }

  object Wildcard
  {
    def unapply(segment: String): Option[String] = {
      if (segment == "*") Some(segment) else None
    }
  }

  object Remainder
  {
    def unapply(path: String): Option[(String, String)] = {
      segments(path).headOption map { (_, segments(path).tail.mkString("/")) }
    }
  }

  object WildcardValue
  {
    def unapply(path: String): Option[String] = {
      if (path.dropWhile(_ == '/').nonEmpty) Some(path.dropWhile(_ == '/')) else None
    }
  }
}
