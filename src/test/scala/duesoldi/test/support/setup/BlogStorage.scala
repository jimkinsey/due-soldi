package duesoldi.test.support.setup

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import duesoldi.Env
import duesoldi.test.support.httpclient.BasicAuthorization
import duesoldi.test.support.app.ServerRequests

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

object BlogStorage
{
  val entry = EntryBuilder()

  case class EntryBuilder(
    id: String = "id",
    content: String = "# Title",
    lastModified: ZonedDateTime = ZonedDateTime.now(),
    description: Option[String] = None
  ) {
    def withId(id: String): EntryBuilder = copy(id = id)
    def withDescription(description: String): EntryBuilder = copy(description = Some(description))
    def withContent(content: String): EntryBuilder = copy(content = content)

    lazy val toYaml: String = {
      s"""id: $id
         |description: ${description.getOrElse("")}
         |published: ${lastModified.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)}
         |content: |
         |${content.lines.map(line => s"    $line").mkString("\n")}
       """.stripMargin
    }
  }

  implicit def tupleToBuilder(tuple: (String, String)): EntryBuilder = tuple match {
    case (id, content) => EntryBuilder(id, content)
  }

  implicit def tuple3ToBuilder(tuple: (String, String, String)): EntryBuilder = tuple match {
    case (time, id, content) => EntryBuilder(id, content, ZonedDateTime.parse(time))
  }

  def blogEntry(entry: EntryBuilder)(implicit executionContext: ExecutionContext) = blogEntries(entry)

  def blogEntries(entries: EntryBuilder*)(implicit executionContext: ExecutionContext) = new AsyncSetup {
    override def setup(env: Env): Future[Env] = {
      implicit val e: Env = env
      val user :: password :: Nil = env("ADMIN_CREDENTIALS").split(":").toList
      Future.sequence(
        entries.map(entry => ServerRequests.put(s"/admin/blog/${entry.id}", entry.toYaml, BasicAuthorization(user, password)))
      ) map ( _ => Map.empty )
    }
  }
}
