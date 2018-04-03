package duesoldi.rendering

import java.io.{BufferedReader, InputStreamReader}

import bhuj.Mustache
import duesoldi.furniture.CurrentUrlPath

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

object Renderer
{
  import bhuj.context.ContextImplicits._

  def render(furniturePath: CurrentUrlPath)(implicit ec: ExecutionContext): Render = {
    new Mustache(getTemplate, Map("furniture" -> { (path: String, _: String => bhuj.Result) => Future.successful(furniturePath(path).map(_._1)) }) ).renderTemplate
  }

  def getTemplate(name: String): Future[Option[String]] = Future.fromTry( Try {
    val stream = this.getClass.getClassLoader.getResourceAsStream(s"templates/$name.mustache")
    val reader = new BufferedReader(new InputStreamReader(stream))
    Option(reader.lines().toArray.mkString("\n"))
  })

}
