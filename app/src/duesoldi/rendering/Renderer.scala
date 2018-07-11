package duesoldi.rendering

import java.io.File
import java.nio.file.{Files, Path}

import bhuj.Mustache
import duesoldi.furniture.CurrentPathAndContent
import hammerspace.resources.Resources

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object Renderer
{
  import bhuj.context.ContextImplicits._

  def render(getTemplate: GetTemplate, furniturePath: CurrentPathAndContent)(implicit ec: ExecutionContext): Render = {
    new Mustache(getTemplate, Map("furniture" -> { (path: String, _: String => bhuj.Result) => Future.successful(furniturePath(path).map(_.path)) }) ).renderTemplate
  }

  val getTemplateFromResources: GetTemplate = (name) => Future.fromTry(
    Resources
      .loadBytes(this.getClass, s"templates/$name.mustache")
      .flatMap(bytes => Try { Some(new String(bytes, "UTF-8")) })
  )

  def getTemplateFromPath(path: String): GetTemplate = (name) => Future.fromTry {
    Try(Files.readAllLines(new File(s"$path/$name.mustache").toPath).toArray.mkString) match {
      case Success(content) => Try(Some(content))
      case Failure(_) => Try(None)
    }
  }
}
