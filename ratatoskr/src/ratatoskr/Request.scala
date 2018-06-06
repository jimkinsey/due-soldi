package ratatoskr

import hammerspace.testing.StreamHelpers._

case class Request(method: Method, url: String, body: Stream[Byte] = Stream.empty, headers: Headers = EmptyHeaders)

object RequestBuilding
{
  implicit def methodToBuilder(method: Method): RequestBuilder = RequestBuilder(Request(method, "/"))

  implicit class RequestBuilder(request: Request)
  {
    def apply(url: String): Request = {
      request.copy(url = url)
    }

    def apply(url: String, body: String): Request = {
      request.copy(url = url, body = body.asByteStream("UTF-8"))
    }

    def header(header: (String, String)): Request = {
      request.copy(headers = addHeader(header, request.headers))
    }
  }
}
