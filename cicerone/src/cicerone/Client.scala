package cicerone

import cicerone.HttpConnection._
import ratatoskr.{Request, Response}

import scala.concurrent.{ExecutionContext, Future}

object Client
{
  val defaultConfiguration = Configuration()
}

class Client(configuration: Configuration = Client.defaultConfiguration)
{
  def send(request: Request)
          (implicit executionContext: ExecutionContext): Future[Either[Failure, Response]] = Future {

    for {
      connection <- open(request.url)
      _ = configure(connection, configuration)
      _ <- applyRequest(connection, request).toLeft({})
      _ <- connect(connection).toLeft({})
      response <- retrieveResponse(connection)
    } yield {
      response
    }

  }
}
