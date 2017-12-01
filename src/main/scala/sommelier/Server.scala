package sommelier

import java.net.{InetSocketAddress, ServerSocket}
import java.util.Scanner

import com.sun.net.httpserver.{HttpExchange, HttpHandler, HttpServer}
import sommelier.ApplyMiddleware.{applyIncoming, applyOutgoing}
import sommelier.ApplyRoutes.applyRoutes

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext
import scala.util.Try
import scala.util.matching.Regex

trait Server
{
  def port: Int
  def halt(): Unit
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

      val serverPort = port.getOrElse(randomPort)
      val server = HttpServer.create(new InetSocketAddress(serverPort), 0)
      server.createContext("/", new Router(routes, middleware))
      server.setExecutor(null); // creates a default executor
      server.start()

      new Server {
        val port: Int = serverPort
        def halt(): Unit = {
          server.stop(0)
        }
      }
    })
  }

  class Router(routes: Seq[Route], middleware: Seq[Middleware])
              (implicit executionContext: ExecutionContext)
  extends HttpHandler
  {
    def handle(exchange: HttpExchange): Unit = {
      Try {
        for {
          finalRequest <- applyIncoming(middleware)(getRequest(exchange))
          interimResponse <- applyRoutes(routes)(finalRequest) recover { _.response }
          finalResponse <- applyOutgoing(middleware)(finalRequest, interimResponse)
        } yield {
          send(exchange)(finalResponse)
        }
      } recover {
        case ex =>
          ex.printStackTrace()
          send(exchange)(Response(500, Some("Internal Server Error")))
      }
    }
  }

  def getMethod(exchange: HttpExchange): Method = {
    exchange.getRequestMethod match {
      case "HEAD" => Method.GET
      case method => Method(method)
    }
  }

  def getRequest(exchange: HttpExchange): Request = {
    Request(
      method = getMethod(exchange),
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
        .foldLeft[Map[String, Seq[String]]](Map.empty) {
          case (acc, QueryParam(key, value)) if acc.contains(key) =>
            acc ++ Map(key -> (acc(key) :+ value))
          case (acc, QueryParam(key, value)) =>
            acc ++ Map(key -> Seq(value))
          case (acc, _) =>
            acc
        }
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
    response.headers.foreach { case (key, value) =>
      exchange.getResponseHeaders.add(key, value)
    }
    if (exchange.getRequestMethod != "HEAD") {
      exchange.sendResponseHeaders(response.status, response.body.map(_.getBytes("UTF-8").length.toLong).getOrElse(0L))
      val os = exchange.getResponseBody
      response.body.foreach { body =>
        os.write(body.getBytes())
      }
      os.close()
    }
    else {
      exchange.sendResponseHeaders(response.status, -1)
    }
  }
}
