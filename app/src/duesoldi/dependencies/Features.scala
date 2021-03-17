package duesoldi.dependencies

import duesoldi.Env
import duesoldi.dependencies.Injection.Inject

object Features {
  def forFeature[T](name: String)(ifOn: => T, ifOff: => T): Inject[T] = (config) => {
    config.features.get(name) match {
      case Some(true) => ifOn
      case _ => ifOff
    }
  }

  def featureStatuses(env: Env): Map[String, Boolean] = {
    env.collect {
      case (key, value) if key.startsWith("FEATURE_") => key.substring("FEATURE_".length) -> (value == "on")
    }
  }
}
