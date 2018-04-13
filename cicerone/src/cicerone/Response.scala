package cicerone

case class Response(status: Int, headers: Headers, body: Stream[Byte])