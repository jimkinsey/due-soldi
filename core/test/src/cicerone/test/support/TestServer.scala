package cicerone.test.support

import java.io.InputStream
import java.net.{InetSocketAddress, ServerSocket}

import com.sun.net.httpserver.{HttpExchange, HttpServer}
import com.sun.net.httpserver.HttpHandler
import duesoldi.streams.InputStreams

import StreamHelpers._

import scala.util.{Failure, Success, Try}

object TestServer
{
  type Headers = Map[String, Seq[String]]

  implicit def tuple2ToResponse(tuple: (Int, String)): Response = Response(tuple._1, Some(tuple._2))
  implicit def tuple3ToResponse(tuple: (Int, String, Headers)): Response = Response(tuple._1, Some(tuple._2), tuple._3)

  case class Request(method: String, path: String, body: Option[String] = None, headers: Headers = Map.empty)
  case class Response(status: Int, body: Option[String] = None, headers: Headers = Map.empty)
  case class ServerInfo(port: Int)

  object GET
  {
    def unapply(request: Request): Option[String] = if (request.method == "GET") Some(request.path) else None
  }
  object POST
  {
    def unapply(request: Request): Option[String] = if (request.method == "POST") Some(request.path) else None
  }

  def withServer[T](routing: PartialFunction[Request, Response])(block: ServerInfo => T): T = {
    val server = {
      val socket = new ServerSocket(0)
      val port = socket.getLocalPort
      socket.close()
      HttpServer.create(new InetSocketAddress(port), 0)
    }

    server.createContext("/", new HttpHandler {
      override def handle(httpExchange: HttpExchange): Unit = {
        val body = InputStreams.toByteStream(httpExchange.getRequestBody).asString
        val request: Request = Request(httpExchange.getRequestMethod, httpExchange.getRequestURI.getPath, Some(body)) // TODO, headers
        if (routing.isDefinedAt(request)) {
          Try {
            val Response(status, body, headers) = routing(request)
            headers.foreach { case (key, values) =>
              values.foreach { value =>
                httpExchange.getResponseHeaders.add(key, value)
              }
            }
            httpExchange.sendResponseHeaders(status, body.map(_.getBytes.length.toLong).getOrElse(0L))
            body.foreach { content =>
              val bodyOut = httpExchange.getResponseBody
              bodyOut.write(content.getBytes)
              bodyOut.close()
            }
          } recover {
            case exception =>
              println(s"Test server routing failed - ${exception.getMessage}")
              httpExchange.sendResponseHeaders(500, -1)
          }
        }
        else {
          httpExchange.sendResponseHeaders(404, -1)
        }
      }
    })
    server.setExecutor(null)
    server.start()

    Try {
      block(ServerInfo(server.getAddress.getPort))
    } match {
      case Success(result) => result
      case Failure(exception) =>
        server.stop(0)
        throw exception
    }
  }
}
