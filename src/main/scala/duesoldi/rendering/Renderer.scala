package duesoldi.rendering

import bhuj.MustacheBuilder.mustacheRenderer

import scala.concurrent.{ExecutionContext, Future}

class Renderer(implicit ec: ExecutionContext) {
  import bhuj.context.ContextImplicits._

  def render(viewName: String, model: PageModel): Future[Either[Renderer.Failure, String]] = {
    mustache.renderTemplate(viewName, model) map {
      case Left(_)        => Left(new Renderer.Failure {})
      case Right(success) => Right(success)
    }
  }

  private lazy val mustache = mustacheRenderer.withTemplatePath("src/main/resources/templates").withoutCache
}

object Renderer {
  sealed trait Failure
}