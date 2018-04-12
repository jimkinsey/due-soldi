package cicerone.test.support

import java.net.{InetSocketAddress, ServerSocket}

import cicerone.test.support.StreamHelpers._
import com.sun.net.httpserver.{HttpExchange, HttpHandler, HttpServer}
import duesoldi.streams.InputStreams

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

object TestServer
{
  type Headers = Map[String, Seq[String]]

  implicit def tuple2ToResponse(tuple: (Int, String)): Response = Response(tuple._1, Some(tuple._2))
  implicit def tuple3ToResponse(tuple: (Int, String, Headers)): Response = Response(tuple._1, Some(tuple._2), tuple._3)
  implicit def statusAndHeaderToResponse(statusAndHeader: (Int, (String, String))): Response = Response(statusAndHeader._1, headers = Map(statusAndHeader._2._1 -> Seq(statusAndHeader._2._2)))

  case class Request(method: String, path: String, body: Option[String] = None, headers: Headers = Map.empty)
  case class Response(status: Int, body: Option[String] = None, headers: Headers = Map.empty)
  case class ServerInfo(port: Int)

  sealed abstract class MethodMatcher(methods: String*)
  {
    def unapply(request: Request): Option[String] = if (methods contains request.method) Some(request.path) else None
  }
  object GET extends MethodMatcher("GET", "HEAD")
  object POST extends MethodMatcher("POST")
  object PUT extends MethodMatcher("PUT")
  object DELETE extends MethodMatcher("DELETE")

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
        val headers: Headers = {
          val rawHeaders = httpExchange.getRequestHeaders.entrySet().asScala
          rawHeaders.foldLeft[Headers](Map.empty) {
            case (acc, entry) => acc ++ Map(entry.getKey -> entry.getValue.asScala)
          }
        }
        val request: Request = Request(httpExchange.getRequestMethod, httpExchange.getRequestURI.getPath, Some(body), headers)
        if (routing.isDefinedAt(request)) {
          Try {
            val Response(status, body, headers) = routing(request)
            headers.foreach { case (key, values) =>
              values.foreach { value =>
                httpExchange.getResponseHeaders.add(key, value)
              }
            }
            httpExchange.sendResponseHeaders(status, body.map(_.getBytes.length.toLong).getOrElse(-1))
            if (request.method != "HEAD" && body.exists(_.nonEmpty)) {
              body.foreach { content =>
                val bodyOut = httpExchange.getResponseBody
                bodyOut.write(content.getBytes)
                bodyOut.close()
              }
            }
          } recover {
            case exception =>
              exception.printStackTrace()
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
