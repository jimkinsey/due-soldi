package duesoldi

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.{ActorMaterializer, Materializer}
import akka.stream.scaladsl.Flow
import com.ning.http.client.Response
import org.scalatest.AsyncFunSpec
import dispatch.{url, Http => HttpReq}
import org.scalatest.Matchers._

import scala.concurrent.Future

class PingPongTests extends AsyncFunSpec with Routing {
  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()

  describe("/ping") {

    it("returns 'pong'") {
      for {
        server <- bind(routes, 6666)
        res    <- get("/ping", server)
      } yield {
        res.getResponseBody shouldBe "pong"
      }
    }

  }

  describe("/pong") {

    it("returns 'ping'") {
      for {
        server <- bind(routes, 6667)
        res    <- get("/pong", server)
      } yield {
        res.getResponseBody shouldBe "ping"
      }
    }

  }

  private def bind(handler: Flow[HttpRequest, HttpResponse, Any], port: Int)(implicit fm: Materializer): Future[ServerBinding] = {
    Http().bindAndHandle(handler, "localhost", port)
  }

  private def get(path: String, server: ServerBinding): Future[Response] = {
    HttpReq(url(s"http://localhost:${server.localAddress.getPort}$path"))
  }

}
