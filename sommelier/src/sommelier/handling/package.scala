package sommelier

import sommelier.routing.RequestMatcher

package object handling
{
  case class Context(request: Request, matcher: RequestMatcher)
}

case class UploadedFile(filename: String, contentType: String, data: Stream[Byte])