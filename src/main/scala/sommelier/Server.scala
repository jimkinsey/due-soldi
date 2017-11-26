package sommelier

import java.io.InputStream
import java.net.InetSocketAddress
import java.util.Scanner

import com.sun.net.httpserver.{HttpExchange, HttpHandler, HttpServer}

import scala.collection.JavaConverters._
import scala.util.Try
import scala.util.matching.Regex

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
      path = exchange.getRequestURI.getPath,
      queryParams = queryParams(exchange),
      headers = headers(exchange),
      accept = Option(exchange.getRequestHeaders.getFirst("Accept")),
      body = body(exchange)
    )
  }

  def queryParams(exchange: HttpExchange): Map[String, Seq[String]] = {
    Option(exchange.getRequestURI.getQuery)
      .map(_
        .split('&')
        .collect {
          case QueryParam(key, value) => key -> Seq(value) // FIXME
        }
        .toMap
      )
      .getOrElse(Map.empty)
  }

  lazy val QueryParam: Regex = """^(.+)=(.+)$""".r

  def headers(exchange: HttpExchange): Map[String, Seq[String]] = {
    exchange
      .getRequestHeaders
      .entrySet().asScala
      .toSeq
      .map(entry => entry.getKey -> entry.getValue.asScala)
      .toMap
  }

  def body(exchange: HttpExchange): Option[String] = {
    exchange.getRequestMethod match {
      case "GET" | "HEAD" | "DELETE" => None
      case _ =>
        val requestBody = exchange.getRequestBody
        val s = new Scanner(requestBody).useDelimiter("\\A")
        val result = if (s.hasNext) Some(s.next) else None
        requestBody.close()
        result
    }
  }

  def send(exchange: HttpExchange)(response: Response): Unit = {
    response.contentType.foreach { contentType =>
      exchange.getResponseHeaders.add("Content-Type", contentType)
    }
    response.location.foreach { contentType =>
      exchange.getResponseHeaders.add("Location", contentType)
    }
    exchange.sendResponseHeaders(response.status, response.body.map(_.length.toLong).getOrElse(0L))
    val os = exchange.getResponseBody
    response.body.foreach { body =>
      os.write(body.getBytes())
    }
    os.close()
  }
}
