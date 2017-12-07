package sommelier

import java.net.{InetSocketAddress, ServerSocket}

import com.sun.net.httpserver.{HttpExchange, HttpServer}
import sommelier.implementation.HttpExchangeMessageContext

import scala.concurrent.ExecutionContext
import scala.util.Try

trait Server
{
  def port: Int
  def halt(): Unit
  def subscribe: Subscriber => Unit
}

object Server
{
  def start(routes: Seq[Route], middleware: Seq[Middleware] = Seq.empty, host: String = "localhost", port: Option[Int] = None)
           (implicit executionContext: ExecutionContext): Try[Server] = {
    Try({
      // might need to look into synchronising this
      lazy val randomPort: Int = {
        val socket = new ServerSocket(0)
        val socketPort = socket.getLocalPort
        socket.close()
        socketPort
      }

      implicit val bus: EventBus = new EventBus

      val serverPort = port.getOrElse(randomPort)
      val server = HttpServer.create(new InetSocketAddress(serverPort), 0)
      server.createContext("/", (httpExchange: HttpExchange) => {
        Router.complete(routes, middleware)(new HttpExchangeMessageContext(httpExchange))
      })
      server.setExecutor(null); // creates a default executor
      server.start()

      new Server {
        val port: Int = serverPort
        def halt(): Unit = {
          bus.publish(HaltRequested)
          server.stop(0)
        }
        val subscribe: (Subscriber) => Unit = bus.subscribe
      }
    })
  }
}
