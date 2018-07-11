package ratatoskr

import java.net.URLEncoder

import hammerspace.testing.StreamHelpers._

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

    def cookie(cookie: Cookie): Request = {
      header(cookie.toRequestHeader)
    }

    def content(content: String): Request = {
      request.copy(body = content.asByteStream("UTF-8"))
    }

    def formValue(formValue: (String, String)): Request = {
      request.copy(body = addParam(formValue, request.body))
    }

    def formValues(formValues: Map[String, Seq[String]]): Request = {
      formValues.foldLeft[Request](request) {
        case (req, (name, values)) => req.formValue(name -> values.head)
      }
    }
  }

  private def addParam(param: (String, String), body: Body) = {
    val (name, value) = param
    val separator = if (body.isEmpty) "" else "&"
    (body.asString + s"$separator$name=${URLEncoder.encode(value, "UTF-8")}").asByteStream("UTF-8")
  }
}
