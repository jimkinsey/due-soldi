package cicerone

import cicerone.HttpConnection._

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}

class Client(connectTimeout: Duration = Duration(5, "seconds"))
{
  def send(request: Request)
          (implicit executionContext: ExecutionContext): Future[Either[Failure, Response]] = Future {

    for {
      connection <- open(request.url)
      _ = configure(connection, HttpConnection.Configuration(connectTimeout))
      _ <- applyRequest(connection, request).toLeft({})
      _ <- connect(connection).toLeft({})
      response <- retrieveResponse(connection)
    } yield {
      response
    }

  }
}
