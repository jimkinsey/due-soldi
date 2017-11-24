package sommelier

import scala.util.{Failure, Success}

object App
{
  import Routing._

  def main(args: Array[String]): Unit = {
    Server.start(Seq(internalStatus, journalPage), port = Some(1984)) match {
      case Success(server) =>
        println(s"Monsieur! A fruity little server is available on ${server.port}. A fine vintage!")
      case Failure(exception) =>
        exception.printStackTrace()
    }
  }

  lazy val internalStatus =
    GET("/internal/status") respond { _ => 200 }

  lazy val journalPage =
    GET("/journal/:id") respond { _ =>
      200("<title>Journal of blah blah blah</title>")
    }

}
