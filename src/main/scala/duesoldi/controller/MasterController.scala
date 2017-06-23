package duesoldi.controller

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import duesoldi._
import duesoldi.assets.HttpAssetStore
import duesoldi.config.Configured
import duesoldi.markdown.MarkdownParser
import duesoldi.rendering.Renderer
import duesoldi.storage._

import scala.concurrent.ExecutionContext

class MasterController(val env: Env)(implicit val executionContext: ExecutionContext, val materializer: Materializer, val actorSystem: ActorSystem) extends Controller
  with Configured
  with FurnitureRoutes
  with MetricsRoutes
  with BlogRoutes
  with RobotsRoutes
  with BlogEditingRoutes
  with DebugRoutes {

  lazy val blogStore = new JDBCBlogStore(config.jdbcConnectionDetails, new MarkdownParser)
  lazy val renderer = new Renderer
  lazy val accessRecordStore =  new JDBCAccessRecordStore(config.jdbcConnectionDetails)
  lazy val assetStore = new HttpAssetStore(config.imageBaseUrl)

  def routes = furnitureRoutes ~ blogRoutes ~ metricsRoutes ~ robotsRoutes ~ blogEditingRoutes ~ debugRoutes

}
