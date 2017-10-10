package duesoldi.rendering

import bhuj.MustacheBuilder.mustacheRenderer

import scala.concurrent.{ExecutionContext, Future}

class Renderer(implicit ec: ExecutionContext) {
  import bhuj.context.ContextImplicits._

  def render(viewName: String, model: PageModel): Future[Either[bhuj.Failure, String]] = {
    mustache.renderTemplate(viewName, model)
  }

  private lazy val mustache = mustacheRenderer.withTemplatePath("src/main/resources/templates").withoutCache
}
