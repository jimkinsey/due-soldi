package duesoldi.controller

import akka.http.scaladsl.server.Directives._
import duesoldi._
import duesoldi.config.Configured
import duesoldi.markdown.MarkdownParser
import duesoldi.rendering.Renderer
import duesoldi.storage.{BlogStore, FilesystemMarkdownSource, InMemoryAccessRecordStore}

import scala.concurrent.ExecutionContext

class MasterController(val env: Env)(implicit val executionContext: ExecutionContext) extends Configured with FurnitureRoutes with MetricsRoutes with BlogRoutes {

  lazy val blogStore = new BlogStore(new FilesystemMarkdownSource(config.blogStorePath), new MarkdownParser)
  lazy val renderer = new Renderer
  lazy val accessRecordStore =  new InMemoryAccessRecordStore

  def routes = furnitureRoutes ~ blogRoutes ~ metricsRoutes

}
