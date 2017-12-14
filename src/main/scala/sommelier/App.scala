package sommelier

import scala.util.{Failure, Success}
//
//object App
//{
//  import scala.concurrent.ExecutionContext.Implicits.global
//
//  def main(args: Array[String]): Unit = {
//    Server.start(Seq(TestController), port = args.headOption.map(_.toInt)) match {
//      case Success(server) =>
//        Runtime.getRuntime.addShutdownHook(new Thread {
//          override def run(): Unit = {
//            server.halt()
//          }
//        })
//        println(s"Monsieur! A fruity little server is available on ${server.port}. A fine vintage!")
//      case Failure(exception) =>
//        exception.printStackTrace()
//    }
//  }
//
//  // todo more tests
//  // todo restructure - DSL on sommelier package object, implementation stuff in packages
//  // todo stress testing
    // todo events = dearboy, injection = beefish
// todo why 405 instead of 404 on so many paths?
// todo access recording via event handler - might require starting new app for each test
//
//}

object TestController
extends Controller
{
  import Routing._
  import Unpacking._

  import scala.concurrent.ExecutionContext.Implicits.global

  GET("/internal/status") ->- { _ => 200 }

  GET("/journal/:id") ->- { implicit context =>
    for {
      id <- pathParam[Int]("id")
      journal <- JournalDataStore.get(id) rejectWith { 404 (s"Journal with ID $id not found") }
    } yield {
      200 (s"<h1>${journal.name}</h1>")
    }
  }

  GET("/journal/:id").Accept("application/json") ->- { implicit context =>
    for {
      id <- pathParam[Int]("id")
      journal <- JournalDataStore.get(id) rejectWith { 404 (s"Journal with ID $id not found") }
    } yield {
      200 (s"""{ "name": "${journal.name}" }""") ContentType "application/json"
    }
  }

  POST("/count") ->- { implicit context =>
    for {
      doc <- body[String]
    } yield {
      200 (s"${doc.length} chars") ContentType "text/plain"
    }
  }

  GET("/headers") ->- { implicit context =>
    val content = context.request.headers.map { case (k,v) => s"$k: ${v mkString ","}" } mkString "\n"
    200 (content) ContentType "text/plain"
  }

  GET("/ua") ->- { implicit context =>
    for {
      ua <- header("User-agent")
    } yield {
      200 (s"Your user-agent is ${ua.headOption.getOrElse("n/a")}")
    }
  }

  GET("/redirect") ->- { implicit context =>
    for {
      loc <- query[String]("loc")
      uri <- loc.headOption rejectWith { 400 ("Need a location to redirect to!") }
    } yield {
      302 Location uri
    }
  }

  GET("/secret").Authorization(Basic("user", "12345", "access to secrets")) ->- { _ =>
    200 ("i know nothing about security")
  }

}