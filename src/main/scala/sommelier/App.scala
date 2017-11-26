package sommelier

import scala.util.{Failure, Success}

object App
{
  import Routing._
  import Unpacking._

  def main(args: Array[String]): Unit = {
    Server.start(Seq(internalStatus, journalData, journalPage, count), port = args.headOption.map(_.toInt)) match {
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
    GET("/journal/:id") Accept "application/json" respond { implicit context =>
      for {
        id <- pathParam[Int]("id")
        journal <- JournalDataStore.get(id) rejectWith { 404 (s"Journal with ID $id not found") }
      } yield {
        200 (s"""{ "name": "${journal.name}" }""") ContentType "application/json"
      }
    }

  lazy val count =
    POST("/count") respond { implicit context =>
      for {
        doc <- body[String]
      } yield {
        200 (s"${doc.length} chars") ContentType "text/plain"
      }
    }

  // TESTS!!!
  // todo HEAD, basic auth, redirects, getting headers, *** async ***, middleware
  // less urgent: event handlers for logging, better rejections
  //   simpler route objects, reversable routes
  //   body unpacker should take content type as arg? or type param?


}
