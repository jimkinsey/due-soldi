package duesoldi.rendering

import bhuj.MustacheBuilder.mustacheRenderer
import duesoldi.furniture.CurrentUrlPath

import scala.concurrent.{ExecutionContext, Future}

object Renderer
{
  import bhuj.context.ContextImplicits._

  def render(furniturePath: CurrentUrlPath)(implicit ec: ExecutionContext): Render = {
    mustacheRenderer
      .withHelpers(
        "furniture" -> { (path, _) => Future.successful(furniturePath(path).map(_._1)) }
      )
      .withTemplatePath("core/resources/templates")
      .withoutCache
      .renderTemplate
  }

}
