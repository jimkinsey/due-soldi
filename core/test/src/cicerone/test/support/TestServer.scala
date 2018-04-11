package cicerone.test.support

import com.sun.net.httpserver.HttpHandler

import scala.util.{Failure, Success, Try}

object TestServer
{
  import java.net.{InetSocketAddress, ServerSocket}

  import com.sun.net.httpserver.{HttpExchange, HttpServer}

  type Request = (String, String)
  type Headers = Map[String, Seq[String]]

  implicit def tuple2ToResponse(tuple: (Int, String)): Response = Response(tuple._1, Some(tuple._2))
  implicit def tuple3ToResponse(tuple: (Int, String, Headers)): Response = Response(tuple._1, Some(tuple._2), tuple._3)

  case class Response(status: Int, body: Option[String] = None, headers: Headers = Map.empty)
  case class ServerInfo(port: Int)

  def withServer[T](routing: PartialFunction[Request, Response])(block: ServerInfo => T): T = {
    val server = {
      val socket = new ServerSocket(0)
      val port = socket.getLocalPort
      socket.close()
      HttpServer.create(new InetSocketAddress(port), 0)
    }

    server.createContext("/", new HttpHandler {
      override def handle(httpExchange: HttpExchange): Unit = {
        val request: Request = (httpExchange.getRequestMethod, httpExchange.getRequestURI.getPath)
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
