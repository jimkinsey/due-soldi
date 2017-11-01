package duesoldi.rendering

import bhuj.MustacheBuilder.mustacheRenderer
import bhuj.Result

import scala.concurrent.{ExecutionContext, Future}

object Renderer
{
  import bhuj.context.ContextImplicits._

  def render(implicit ec: ExecutionContext): (String, PageModel) => Future[Result] = mustache.renderTemplate

  private lazy val mustache = mustacheRenderer.withTemplatePath("src/main/resources/templates").withoutCache
}
