package ratatoskr

case class Request(method: Method, url: String, body: Stream[Byte] = Stream.empty, headers: Headers = EmptyHeaders)
