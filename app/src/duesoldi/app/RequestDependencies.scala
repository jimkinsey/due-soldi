package duesoldi.app

import duesoldi.app.RequestConfig._
import duesoldi.config.Config
import duesoldi.dependencies.Injection.Inject
import sommelier.Context
import sommelier.routing.Result

object RequestDependencies
{
  def provided[T](implicit injector: Inject[T], appConfig: Config, context: Context): Result[T] =
    requestConfig recover (_ => appConfig) map injector
}
