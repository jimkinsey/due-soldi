package sommelier

import sommelier.routing.RequestMatcher

package object handling
{
  case class Context(request: Request, matcher: RequestMatcher)
}
