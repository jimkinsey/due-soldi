package duesoldi.controller

import akka.http.scaladsl.server.Directives._
import duesoldi._
import duesoldi.config.Configured
import duesoldi.markdown.MarkdownParser
import duesoldi.rendering.Renderer
import duesoldi.storage._

import scala.concurrent.ExecutionContext

class MasterController(val env: Env)(implicit val executionContext: ExecutionContext) extends Configured with FurnitureRoutes with MetricsRoutes with BlogRoutes with RobotsRoutes {

  private lazy val markdownSource: MarkdownSource = new JDBCMarkdownSource(config.jdbcDatabaseUrl, config.jdbcDatabaseUsername, config.jdbcDatabasePassword)

  lazy val blogStore = new BlogStore(markdownSource, new MarkdownParser)
  lazy val renderer = new Renderer
  lazy val accessRecordStore =  new JDBCAccessRecordStore(config.jdbcDatabaseUrl, config.jdbcDatabaseUsername, config.jdbcDatabasePassword)

  def routes = furnitureRoutes ~ blogRoutes ~ metricsRoutes ~ robotsRoutes

}
