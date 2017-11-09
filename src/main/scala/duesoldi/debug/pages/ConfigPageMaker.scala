package duesoldi.debug.pages

import duesoldi.config.Config
import duesoldi.config.EnvironmentalConfig.{nonSensitive, toEnv}

object ConfigPageMaker
{
  def makeConfigPage(config: Config): MakeConfigPage = { () =>
    nonSensitive(toEnv(config)).map { case (key, value) => s"$key=$value" } mkString "\n"
  }
}
