package sommelier

import sommelier.ApplyMiddleware.{applyIncoming, applyOutgoing}
import sommelier.ApplyRoutes.applyRoutes

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration
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
              (implicit executionContext: ExecutionContext, bus: EventBus): Unit = {
    val start = System.nanoTime()

    def handleException: PartialFunction[Throwable, Result[Response]] = {
      case ex =>
        bus.publish(ExceptionWhileRouting(context.get, ex))
        SyncResult.Accepted(Response(500, Some("Internal Server Error")))
    }

    def sendResponse(result: Result[Response]): Unit = result match {
      case sync: SyncResult[Response] =>
        sync.recover(rejection => rejection.response).map { response =>
          bus.publish(Completed(context.get, response, Duration.fromNanos(System.nanoTime())))
          context.send(response)
        }
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
