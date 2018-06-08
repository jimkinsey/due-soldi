package ratatoskr

import hammerspace.testing.StreamHelpers._

object ResponseBuilding
{
  implicit def statusToResponseBuilder(status: Int): ResponseBuilder = ResponseBuilder(Response(status))

  implicit class ResponseBuilder(val response: Response)
  {
    def apply(content: String): Response = this.content(content)
    def apply(content: Array[Byte]): Response = this.content(content)

    def header(header: (String, String)): ResponseBuilder = update(response.copy(headers = addHeader(header, response.headers)))

    def cookie(cookie: Cookie): ResponseBuilder = header("Set-Cookie" -> cookie.format)

    def content(utf8String: String): ResponseBuilder = update(response.copy(body = utf8String.asByteStream("UTF-8")))
    def content(bytes: Array[Byte]): ResponseBuilder = update(response.copy(body = bytes.toStream))

    def WWWAuthenticate(auth: String): ResponseBuilder = header("WWW-Authenticate" -> auth)
    def Location(url: String): ResponseBuilder = header("Location" -> url)
    def ContentType(contentType: String): ResponseBuilder = header("Content-Type" -> contentType)

    private def update(response: Response) = new ResponseBuilder(response)
  }

  implicit def builderToBuilt(builder: ResponseBuilder): Response = builder.response
}
