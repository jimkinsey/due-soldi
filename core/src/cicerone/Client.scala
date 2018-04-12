package cicerone

import cicerone.HttpConnection._

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}

class Client(connectTimeout: Duration = Duration(5, "seconds"))
{
  def GET(url: String)(implicit executionContext: ExecutionContext): Future[Either[Failure, Response]] = send("GET", url)

  def POST(url: String, body: String)(implicit executionContext: ExecutionContext): Future[Either[Failure, Response]] = send("POST", url, Some(body))

  def send(method: String, url: String, body: Option[String] = None)
          (implicit executionContext: ExecutionContext): Future[Either[Failure, Response]] = Future {

    for {
      conn <- open(url)
      _ = configure(conn, HttpConnection.Configuration(connectTimeout))
      _ <- applyRequest(conn, Request(method, url, body)).toLeft({})
      _ <- connect(conn).toLeft({})
      response <- retrieveResponse(conn)
    } yield {
      response
    }

  }
}
