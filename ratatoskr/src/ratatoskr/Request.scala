package ratatoskr

case class Request(method: String, url: String, body: Stream[Byte] = Stream.empty, headers: Headers = EmptyHeaders)
