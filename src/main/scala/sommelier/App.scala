package sommelier

import scala.util.{Failure, Success}

object App
{
  import Routing._
  import Unpacking._

  def main(args: Array[String]): Unit = {
    Server.start(Seq(internalStatus, journalPage), port = args.headOption.map(_.toInt)) match {
      case Success(server) =>
        println(s"Monsieur! A fruity little server is available on ${server.port}. A fine vintage!")
      case Failure(exception) =>
        exception.printStackTrace()
    }
  }

  lazy val internalStatus =
    GET("/internal/status") respond { _ => Right(200) }

  lazy val journalPage =
    GET("/journal/:id") respond { implicit context =>
      for {
        id <- pathParam[Int]("id")
      } yield {
        200 (s"<title>Journal of $id</title>")
      }
    }

}
