package duesoldi.config

import duesoldi._

import scala.concurrent.duration.Duration
import scala.util.Try

trait Configured {

  def env: Env

  lazy val config = Config(
    blogStorePath = env.getOrElse("BLOG_STORE_PATH", "src/main/resources/content"),
    furnitureVersion = env.getOrElse("FURNITURE_VERSION", "LOCAL"),
    furniturePath = env.getOrElse("FURNITURE_PATH", "src/main/resources/furniture"),
    furnitureCacheDuration = env.get("FURNITURE_CACHE_DURATION").flatMap(s => Try(Duration(s)).toOption).getOrElse(Duration.Zero)
  )
}
