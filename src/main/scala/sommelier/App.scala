package sommelier

import scala.util.{Failure, Success}

object App
{
  import Routing._
  import Unpacking._

  def main(args: Array[String]): Unit = {
    Server.start(Seq(internalStatus, journalData, journalPage), port = args.headOption.map(_.toInt)) match {
      case Success(server) =>
        println(s"Monsieur! A fruity little server is available on ${server.port}. A fine vintage!")
      case Failure(exception) =>
        exception.printStackTrace()
    }
  }

  lazy val internalStatus =
    GET("/internal/status") respond { _ => 200 }

  lazy val journalPage =
    GET("/journal/:id") respond { implicit context =>
      for {
        id <- pathParam[Int]("id")
        journal <- JournalDataStore.get(id) rejectWith { 404 (s"Journal with ID $id not found") }
      } yield {
        200 (s"<h1>${journal.name}</h1>")
      }
    }
  lazy val journalData =
    GET("/journal/:id").Accepts("application/json") respond { implicit context =>
      for {
        id <- pathParam[Int]("id")
        journal <- JournalDataStore.get(id) rejectWith { 404 (s"Journal with ID $id not found") }
      } yield {
        200 (s"""{ "name": "${journal.name}" }""").ContentType("application/json")
      }
    }

}
