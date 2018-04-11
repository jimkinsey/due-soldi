package object cicerone {
  sealed trait Failure
  case object ConnectionFailure extends Failure

  type Headers = Map[String,Seq[String]]

  val http = new Client
}
