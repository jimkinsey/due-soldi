package sommelier

import sommelier.ApplyMiddleware.{applyIncoming, applyOutgoing}
import sommelier.ApplyRoutes.applyRoutes

import scala.concurrent.ExecutionContext
import scala.util.Try

trait HttpMessageContext
{
  def get: Request
  def send(response: Response)
}

object Router
{
  def complete(routes: Seq[Route], middleware: Seq[Middleware])
              (context: HttpMessageContext)
              (implicit executionContext: ExecutionContext)
  {
    Try {
      for {
        finalRequest <- applyIncoming(middleware)(context.get)
        interimResponse <- applyRoutes(routes)(finalRequest) recover { _.response }
        finalResponse <- applyOutgoing(middleware)(finalRequest, interimResponse)
      } yield {
        context.send(finalResponse)
      }
    } recover {
      case ex =>
        ex.printStackTrace()
        context.send(Response(500, Some("Internal Server Error")))
    }
  }
}
