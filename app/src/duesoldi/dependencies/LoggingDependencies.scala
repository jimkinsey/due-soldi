package duesoldi.dependencies

import duesoldi.dependencies.Injection.Inject
import duesoldi.logging.Logger

trait LoggingDependencies {
  implicit val logger: Inject[Logger] = {
    config =>
      new Logger(config.loggerName, config.loggingEnabled)
  }
}
