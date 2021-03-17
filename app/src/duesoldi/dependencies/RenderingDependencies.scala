package duesoldi.dependencies

import duesoldi.dependencies.Injection.{Inject, inject, injected}
import duesoldi.furniture.CurrentPathAndContent
import duesoldi.furniture.storage.FurnitureFiles
import duesoldi.logging.Logger
import duesoldi.rendering.{GetTemplate, Renderer}

import scala.concurrent.ExecutionContext

trait RenderingDependencies
  extends LoggingDependencies {

  implicit lazy val furniturePathAndContent: Inject[CurrentPathAndContent] = _ => FurnitureFiles.currentPathAndContent

  implicit lazy val getTemplate: Inject[GetTemplate] = config => {
    config.templatePath match {
      case Some(path) =>
        injected[Logger](logger, config).info(s"Loading mustache templates from '$path'")
        Renderer.getTemplateFromPath(path)
      case _ => Renderer.getTemplateFromResources
    }
  }

  implicit def render(implicit executionContext: ExecutionContext): Inject[duesoldi.rendering.Render] = {
    inject(Renderer.render _)
  }
}
