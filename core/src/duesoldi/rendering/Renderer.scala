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
<<<<<<< 91ca2762ffb85b4f0925a0af496d15c8450c39af:core/src/duesoldi/rendering/Renderer.scala
      .withTemplatePath("core/resources/templates")
=======
      .withTemplatePath("/Users/jimkinsey/Documents/github/due-soldi/core/resources/templates")
>>>>>>> Initial investigation of using Mill to build the app:core/src/duesoldi/rendering/Renderer.scala
      .withoutCache
      .renderTemplate
  }

}
