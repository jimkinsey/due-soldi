package duesoldi.assets

import scala.concurrent.Future

trait AssetStore {
  def asset(path: String): Future[Either[AssetStore.Failure, Asset]]
}

object AssetStore {
  sealed trait Failure
  case object AssetNotFound extends Failure
  case object UpstreamError extends Failure
}