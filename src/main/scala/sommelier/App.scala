package sommelier

import scala.util.{Failure, Success}

object App
{
  import Routing._
  import Unpacking._

  def main(args: Array[String]): Unit = {
    Server.start(Seq(internalStatus, journalData, journalPage, count, headers, ua), port = args.headOption.map(_.toInt)) match {
      case Success(server) =>
        Runtime.getRuntime.addShutdownHook(new Thread {
          override def run(): Unit = {
            println("Shutting down...")
            server.halt()
          }
        })
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

  lazy val headers =
    GET("/headers") respond { implicit context =>
      val content = context.request.headers.map { case (k,v) => s"$k: ${v mkString ","}" } mkString "\n"
      200 (content) ContentType "text/plain"
    }

  lazy val ua =
    GET("/ua") respond { implicit context =>
      for {
        ua <- header("User-agent")
      } yield {
        200 (s"Your user-agent is ${ua.headOption.getOrElse("n/a")}")
      }
    }

  // TESTS!!!
  // todo HEAD, basic auth, redirects, *** async ***, middleware
  // less urgent: event handlers for logging, better rejections
  //   simpler route objects, reversable routes
  //   body unpacker should take content type as arg? or type param?


}
