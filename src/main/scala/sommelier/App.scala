package sommelier

import scala.util.{Failure, Success}

object App
{
  import Routing._
  import Unpacking._
  import scala.concurrent.ExecutionContext.Implicits.global

  def main(args: Array[String]): Unit = {
    Server.start(Seq(internalStatus, journalData, journalPage, count, headers, ua, redirector, secret), port = args.headOption.map(_.toInt)) match {
      case Success(server) =>
        Runtime.getRuntime.addShutdownHook(new Thread { // MAKE THIS WORK!!!
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

  lazy val redirector =
    GET("/redirect") respond { implicit context =>
      for {
        loc <- query[String]("loc")
        uri <- loc.headOption rejectWith { 400 ("Need a location to redirect to!") }
      } yield {
        302 Location uri
      }
    }

  lazy val secret =
    GET("/secret") Authorization Basic("user", "12345", "access to secrets") respond { _ =>
      200 ("i know nothing about security")
    }

  // TESTS!!!
  // todo middleware
  // todo controllers
  // todo tests

}
