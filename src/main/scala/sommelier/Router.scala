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
              (implicit executionContext: ExecutionContext): Unit = {

    def handleException: PartialFunction[Throwable, Result[Response]] = {
      case ex =>
        SyncResult.Accepted(Response(500, Some("Internal Server Error")))
    }

    def sendResponse(result: Result[Response]): Unit = result match {
      case sync: SyncResult[Response] =>
        sync.recover(rejection => rejection.response).map(context.send)
      case AsyncResult(async) =>
        async.recover(handleException).map(sendResponse)
    }

    Try {
      for {
        finalRequest <- applyIncoming(middleware)(context.get)
        interimResponse <- applyRoutes(routes)(finalRequest) recover { _.response }
        finalResponse <- applyOutgoing(middleware)(finalRequest, interimResponse)
      } yield {
        finalResponse
      }
    } recover {
      handleException
    } map {
      sendResponse
    }
  }
}
