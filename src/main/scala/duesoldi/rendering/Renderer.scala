package duesoldi.rendering

import duesoldi.model.BlogEntry

import scala.concurrent.{ExecutionContext, Future}

class Renderer {
  def render(entry: BlogEntry)(implicit ec: ExecutionContext): Future[Either[Renderer.Failure, String]] = {
    Future successful Right(
      <html>
        <head><title>{entry.title}</title></head>
        <body>
          <h1>{entry.title}</h1>
        </body>
      </html>.mkString
    )
  }
}

object Renderer {
  sealed trait Failure
}