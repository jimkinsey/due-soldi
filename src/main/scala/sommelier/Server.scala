package sommelier

import java.io.InputStream
import java.net.InetSocketAddress
import java.util.Scanner

import com.sun.net.httpserver.{HttpExchange, HttpHandler, HttpServer}

import scala.util.Try

trait Server
{
  def port: Int
  def halt(): Unit
}

object Server
{
  def start(routes: Seq[Route], host: String = "localhost", port: Option[Int] = None): Try[Server] = {
    Try({
      val serverPort = port.getOrElse(8000)
      val server = HttpServer.create(new InetSocketAddress(serverPort), 0)
      server.createContext("/", new Router(routes))
      server.setExecutor(null); // creates a default executor
      server.start()

      new Server {
        val port: Int = serverPort
        def halt(): Unit = { server.stop(0) }
      }
    })
  }

  class Router(routes: Seq[Route]) extends HttpHandler {
    def handle(exchange: HttpExchange): Unit = {
      val request = getRequest(exchange)
      routes.toStream.find(_.matcher.matches(request)) match {
        case Some(route) =>
          route
            .handle(Context(request, route.matcher))
            .map(send(exchange))
            .left.map(rejection => send(exchange)(rejection.response))
        case None => send(exchange)(Response(404, Some("Route not matched")))
      }
    }
  }

  def getRequest(exchange: HttpExchange): Request = {
    Request(
      method = Method(exchange.getRequestMethod),
      path = exchange.getRequestURI.toString,
      accept = Option(exchange.getRequestHeaders.getFirst("Accept")),
      body = body(exchange)
    )
  }

  def body(exchange: HttpExchange): Option[String] = {
    exchange.getRequestMethod match {
      case "GET" | "HEAD" | "DELETE" => None
      case _ =>
        val requestBody = exchange.getRequestBody
        val s = new Scanner(requestBody).useDelimiter("\\A")
        val result = if (s.hasNext) Some(s.next) else None
        requestBody.close()
        println(s"REQUEST BODY $result")
        result
    }
  }

  def send(exchange: HttpExchange)(response: Response): Unit = {
    response.contentType.foreach { contentType =>
      exchange.getResponseHeaders.add("Content-Type", contentType)
    }
    exchange.sendResponseHeaders(response.status, response.body.map(_.length.toLong).getOrElse(0L))
    val os = exchange.getResponseBody
    response.body.foreach { body =>
      os.write(body.getBytes())
    }
    os.close()
  }
}
