package ratatoskr

case class Response(status: Int, headers: Headers = EmptyHeaders, body: Stream[Byte] = Stream.empty)