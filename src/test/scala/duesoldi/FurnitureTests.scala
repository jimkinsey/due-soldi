package duesoldi

import java.io.{File, PrintWriter}
import java.time.format.DateTimeFormatter._
import java.util.UUID

import duesoldi.Setup.withSetup
import duesoldi.filesystem.DeleteDir
import duesoldi.test.matchers.CustomMatchers._
import duesoldi.testapp.ServerRequests._
import duesoldi.testapp.TestApp.runningApp
import utest._

import scala.concurrent.Future

object FurnitureTests 
  extends TestSuite
{
  implicit val executionContext = utest.framework.ExecutionContext.RunNow
  val tests = this {
    "furniture requests" - {
      "serve the file from the furniture directory" - {
        withSetup(
          furniture(version = "1.0.0")("chair.txt" -> "four legs, a seat and a back"),
          runningApp
        ) { implicit env =>
          for {
            response <- get("/furniture/1.0.0/chair.txt")
          } yield {
            assert(response.status == 200)
          }
        }
      }
      "404 for a non-existent furniture file" - {
        withSetup(
          furniture(version = "1.0.0")(),
          runningApp
        ) { implicit env =>
          for {
            response <- get("/furniture/1.0.0/two-legged-table.txt")
          } yield {
            assert(response.status == 404)
          }
        }
      }
      "400 for an existing furniture file with the wrong version in the path" - {
        withSetup(
          furniture(version = "5.0.0")("sofa.txt" -> "aaaaahhh..."),
          runningApp
        ) { implicit env =>
          for {
            response <- get("/furniture/4.0.0/sofa.txt")
          } yield {
            assert(response.status == 400)
          }
        }
      }
      "include cache headers when furniture caching is enabled" - {
        withSetup(
          furniture(version = "3.0.0", cacheDuration = Some("1hour"))("cupboard.txt" -> "bare"),
          runningApp
        ) { implicit env =>
          for {
            response <- get("/furniture/3.0.0/cupboard.txt")
          } yield {
            assert(
              response.headers.toSeq.contains("Cache-Control" -> Seq("max-age=3600")),
              response.headers("Expires").head hasDateFormat RFC_1123_DATE_TIME
            )
          }
        }
      }
    }
  }

  def furniture(version: String, cacheDuration: Option[String] = None)(files: (String, String)*) = new SyncSetup {
    lazy val path = s"/tmp/furniture/${UUID.randomUUID().toString.take(6)}"

    override def setup(env: Env) = {
      new File(path).mkdirs()
      files foreach { case (name, content) =>
        val file = new File(s"$path/$name")
        file.getParentFile.mkdirs()
        val writer = new PrintWriter(file)
        writer.write(content)
        writer.close()
      }
      Map("FURNITURE_PATH" -> path) + ("FURNITURE_VERSION" -> version) + ("FURNITURE_CACHE_DURATION" -> cacheDuration.getOrElse("0"))
    }

    override def tearDown = {
      DeleteDir(new File(path).toPath)
    }
  }
}
