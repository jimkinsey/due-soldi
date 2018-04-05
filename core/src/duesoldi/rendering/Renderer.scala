package duesoldi.rendering

import duesoldi.furniture.CurrentPathAndContent
import duesoldi.resources.Resources

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

object Renderer
{
  import bhuj.context.ContextImplicits._

  def render(furniturePath: CurrentPathAndContent)(implicit ec: ExecutionContext): Render = {
    new Mustache(getTemplate, Map("furniture" -> { (path: String, _: String => bhuj.Result) => Future.successful(furniturePath(path).map(_.path)) }) ).renderTemplate
  }

  def getTemplate(name: String): Future[Option[String]] = Future.fromTry(
    Resources
      .loadBytes(s"templates/$name.mustache")
      .flatMap(bytes => Try { Some(new String(bytes, "UTF-8")) })
  )
}
