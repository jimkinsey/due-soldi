package duesoldi.storage

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import duesoldi.testapp.TestServer
import duesoldi.{Controller, Server, Setup, _}

import scala.concurrent.{ExecutionContext, Future}

trait Images {
  implicit def executionContext: ExecutionContext

  import akka.http.scaladsl.model.StatusCode._

  def images(responses: (String, Int)*) = new Setup {
    private var imageServer: Option[Server] = None

    override def setup(env: Env): Future[Env] = {
      TestServer.start(new FakeImagesController(responses, "jimkinsey")) map { server =>
        imageServer = Some(server)
        Map(
          "IMAGE_BASE_URL"  -> s"http://${server.host}:${server.port}/jimkinsey"
        )
      }
    }

    override def tearDown: Future[Unit] = {
      imageServer match {
        case None         => Future.successful({})
        case Some(server) => server.stop()
      }
    }

  }

  private class FakeImagesController(responses: Seq[(String, Int)], pathRoot: String) extends Controller {
    def routes: Route = pathPrefix(pathRoot) {
      get {
        path(RemainingPath) { path =>
          responses.toMap.get(s"/${path.toString}") match {
            case Some(200)    => getFromResource("image.jpg")
            case Some(status) => complete { HttpResponse(status = status) }
            case None         => complete { HttpResponse(status = NotFound) }
          }
        }
      }
    }
  }

}
