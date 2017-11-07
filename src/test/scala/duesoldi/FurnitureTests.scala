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
          furniture("chair.txt" -> "four legs, a seat and a back"),
          runningApp
        ) { implicit env =>
          for {
            file <- furnitureFile("chair.txt")
            response <- get(s"/furniture/${file.lastModified()}/chair.txt")
          } yield {
            assert(response.status == 200)
          }
        }
      }
      "404 for a non-existent furniture file" - {
        withSetup(
          furniture(),
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
          furniture("sofa.txt" -> "aaaaahhh..."),
          runningApp
        ) { implicit env =>
          for {
            file <- furnitureFile("sofa.txt")
            response <- get(s"/furniture/${file.lastModified() + 1}/sofa.txt")
          } yield {
            assert(response.status == 400)
          }
        }
      }
      "include cache headers when furniture caching is enabled" - {
        withSetup(
          furniture("cupboard.txt" -> "bare"),
          runningApp
        ) { implicit env =>
          for {
            file <- furnitureFile("cupboard.txt")
            response <- get(s"/furniture/${file.lastModified()}/cupboard.txt")
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

  def furniture(files: (String, String)*) = new SyncSetup {
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
      Map("FURNITURE_PATH" -> path)
    }

    override def tearDown = {
      DeleteDir(new File(path).toPath)
    }
  }

  def furnitureFile(name: String)(implicit env: Env): Future[File] = {
    Future.successful(new File(s"${env("FURNITURE_PATH")}/$name"))
  }
}
