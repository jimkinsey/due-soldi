package duesoldi.controller

import akka.http.scaladsl.server.Directives._
import duesoldi._
import duesoldi.config.Configured
import duesoldi.markdown.MarkdownParser
import duesoldi.rendering.Renderer
import duesoldi.storage.{BlogStore, FilesystemMarkdownSource}

import scala.concurrent.ExecutionContext

class MasterController(implicit val executionContext: ExecutionContext, val env: Env) extends Configured with FurnitureRoutes with BlogRoutes {

  lazy val blogStore = new BlogStore(new FilesystemMarkdownSource(config.blogStorePath), new MarkdownParser)
  lazy val renderer = new Renderer

  def routes = furnitureRoutes ~ blogRoutes

}
