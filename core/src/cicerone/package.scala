package object cicerone {
  sealed trait Failure
  case object ConnectionFailure extends Failure
  case object MalformedURL extends Failure
  case class UnexpectedException(exception: Exception) extends Failure

  type Headers = Map[String,Seq[String]]

  val http = new Client
}
