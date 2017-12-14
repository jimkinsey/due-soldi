package sommelier

import java.net.{InetSocketAddress, ServerSocket}
import java.util.concurrent.Executor

import com.sun.net.httpserver.{HttpExchange, HttpServer}
import sommelier.implementation.HttpExchangeMessageContext

import scala.concurrent.ExecutionContext
import scala.util.Try

trait Server
{
  def port: Int
  def host: String
  def halt(): Unit
  def subscribe: Subscriber => Unit
}

object Server
{
  private def apply(server: HttpServer, bus: EventBus): Server = {
    new Server {
      def port: Int = server.getAddress.getPort
      val host: String = server.getAddress.getHostName
      def halt(): Unit = {
        bus.publish(HaltRequested)
        server.stop(0)
      }
      val subscribe: (Subscriber) => Unit = bus.subscribe
    }
  }

  private object Lock

  def start(controllers: Seq[Controller] = Seq.empty, routes: Seq[Route] = Seq.empty, middleware: Seq[Middleware] = Seq.empty, host: String = "localhost", port: Option[Int] = None)
           (implicit executionContext: ExecutionContext): Try[Server] = {
    Try({
      val server = Lock.synchronized { // this still isn't working
        lazy val randomPort: Int = {
          val socket = new ServerSocket(0)
          val socketPort = socket.getLocalPort
          socket.close()
          socketPort
        }

        val serverPort = port.getOrElse(randomPort)

        HttpServer.create(new InetSocketAddress(serverPort), 0)
      }

      val allRoutes = routes ++ controllers.flatMap(_.routes)
      val allMiddleware = middleware ++ controllers.flatMap(_.middleware)

      implicit val bus: EventBus = new EventBus

      server.createContext("/", (httpExchange: HttpExchange) => {
        Router.complete(allRoutes, allMiddleware)(new HttpExchangeMessageContext(httpExchange))
      })
      server.setExecutor(null)
      server.start()

      Server(server, bus)
    })
  }
}
