package duesoldi

import java.io.{File, PrintWriter}
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

import duesoldi.storage.DeleteDir
import org.scalatest.AsyncWordSpec

import scala.concurrent.Future
import scala.util.{Success, Try}

class FurnitureTests extends AsyncWordSpec {
  import duesoldi.testapp.TestAppRequest.get
  import org.scalatest.Matchers._

  "furniture requests" must {

    "serve the file from the furniture directory" in {
      withFurniture(version = "1.0.0")("chair.txt" -> "four legs, a seat and a back") {
        get("/furniture/1.0.0/chair.txt") { response =>
          response.status shouldBe 200
        }
      }
    }

    "404 for a non-existent furniture file" in {
      withFurniture(version = "1.0.0")() {
        get("/furniture/1.0.0/two-legged-table.txt") { response =>
          response.status shouldBe 404
        }
      }
    }

    "400 for an existing furniture file with the wrong version in the path" in {
      withFurniture(version = "5.0.0")("sofa.txt" -> "aaaaahhh...") {
        get("/furniture/4.0.0/sofa.txt") { response =>
          response.status shouldBe 400
        }
      }
    }

    "include cache headers when furniture caching is enabled" in {
      withFurniture(version = "1.0.0", cacheDuration = Some("1 hour"))("cupboard.txt" -> "bare") {
        get("/furniture/1.0.0/cupboard.txt") { response =>
          response.headers should contain("Cache-Control" -> Seq("max-age=3600"))
          Try(ZonedDateTime.parse(response.headers("Expires").head, DateTimeFormatter.RFC_1123_DATE_TIME)) should be(a[Success[_]])
        }
      }
    }

  }

  def withFurniture[T <: Future[_]](version: String, cacheDuration: Option[String] = None)(files: (String, String)*)(block: Env => T)(implicit env: Env = Map.empty): T = {
    lazy val path = s"/tmp/furniture/${UUID.randomUUID().toString.take(6)}"
    files foreach { case (name, content) =>
      val file = new File(s"$path/$name")
      file.getParentFile.mkdirs()
      val writer = new PrintWriter(file)
      writer.write(content)
      writer.close()
    }
    val fut = block(env + ("FURNITURE_PATH" -> path) + ("FURNITURE_VERSION" -> version) + ("FURNITURE_CACHE_DURATION" -> cacheDuration.getOrElse("")))
    fut.onComplete( _ => DeleteDir(new File(path).toPath))
    fut
  }
}
