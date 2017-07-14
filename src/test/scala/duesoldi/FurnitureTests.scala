package duesoldi

import java.io.{File, PrintWriter}
import java.time.format.DateTimeFormatter._
import java.util.UUID

import duesoldi.Setup.withSetup
import duesoldi.filesystem.DeleteDir
import duesoldi.testapp.ServerSupport._
import duesoldi.testapp.ServerRequests._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import utest._
import test.matchers.CustomMatchers._

object FurnitureTests 
  extends TestSuite
{
  implicit val executionContext = utest.framework.ExecutionContext.RunNow
  val tests = this {
    "furniture requests" - {
      "serve the file from the furniture directory" - {
        withSetup(furniture(version = "1.0.0")("chair.txt" -> "four legs, a seat and a back")) {
          withServer { implicit server =>
            for {
              response <- get("/furniture/1.0.0/chair.txt")
            } yield {
              assert(response.status == 200)
            }
          }
        }
      }
      "404 for a non-existent furniture file" - {
        withSetup(furniture(version = "1.0.0")()) {
          withServer { implicit server =>
            for {
              response <- get("/furniture/1.0.0/two-legged-table.txt")
            } yield {
              assert(response.status == 404)
            }
          }
        }
      }
      "400 for an existing furniture file with the wrong version in the path" - {
        withSetup(furniture(version = "5.0.0")("sofa.txt" -> "aaaaahhh...")) {
          withServer { implicit server =>
            for {
              response <- get("/furniture/4.0.0/sofa.txt")
            } yield {
              assert(response.status == 400)
            }
          }
        }
      }
      "include cache headers when furniture caching is enabled" - {
        withSetup(furniture(version = "1.0.0", cacheDuration = Some("1 hour"))("cupboard.txt" -> "bare")) {
          withServer { implicit server =>
            for {
              response <- get("/furniture/1.0.0/cupboard.txt")
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
  }

  def furniture(version: String, cacheDuration: Option[String] = None)(files: (String, String)*) = new Setup {
    lazy val path = s"/tmp/furniture/${UUID.randomUUID().toString.take(6)}"

    override def setup(env: Env) = {
      files foreach { case (name, content) =>
        val file = new File(s"$path/$name")
        file.getParentFile.mkdirs()
        val writer = new PrintWriter(file)
        writer.write(content)
        writer.close()
      }
      Future.successful(Map("FURNITURE_PATH" -> path) + ("FURNITURE_VERSION" -> version) + ("FURNITURE_CACHE_DURATION" -> cacheDuration.getOrElse("")))
    }

    override def tearDown: Future[Unit] = {
      Future.successful(DeleteDir(new File(path).toPath))
    }
  }
}
