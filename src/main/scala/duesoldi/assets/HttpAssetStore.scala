package duesoldi.assets

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{HttpMethods, HttpRequest}
import akka.stream.Materializer

import scala.concurrent.{ExecutionContext, Future}

class HttpAssetStore(baseUrl: String)(implicit actorSystem: ActorSystem, materializer: Materializer, executionContext: ExecutionContext) extends AssetStore {
  import scala.concurrent.duration._

  def asset(path: String): Future[Either[AssetStore.Failure, Asset]] = {
    for {
      res <- Http().singleRequest(HttpRequest(
        method = HttpMethods.GET,
        uri = s"$baseUrl$path"
      ))
      data <- res.entity.toStrict(30 seconds).map(_.data.to[Array])
    } yield {
      res.status match {
        case OK =>
          Right(Asset(data = data, contentType = res.entity.contentType.mediaType.value))
        case NotFound =>
          Left(AssetStore.AssetNotFound)
        case _ =>
          Left(AssetStore.UpstreamError)
      }
    }
  }
}
