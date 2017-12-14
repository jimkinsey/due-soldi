package duesoldi.controller

import java.io.File
import java.nio.file.Files

import sommelier.Controller
import sommelier.Routing._
import sommelier.Unpacking._

object LearnJapaneseController
extends Controller
{
  GET("/learn-japanese/") ->- { _ =>
    200 (fileContent("src/main/resources/static/learn-japanese/")("index.html")) ContentType "text/html; charset=UTF-8"
  }

  GET("/learn-japanese/*") ->- { implicit context =>
    for {
      path <- remainingPath
    } yield {
      200 (fileContent("src/main/resources/static/learn-japanese/")(path))
    }
  }

  def fileContent(basePath: String)(path: String): Array[Byte] = {
    Files.readAllBytes(new File(basePath + path).toPath)
  }
}
