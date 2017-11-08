package duesoldi.storage

import java.time.ZonedDateTime

import duesoldi.httpclient.BasicAuthorization
import duesoldi.testapp.ServerRequests
import duesoldi.{AsyncSetup, Env, Setup}

import scala.concurrent.{ExecutionContext, Future}

object BlogStorage
{
  case class EntryBuilder(id: String = "id", content: String = "# Title", lastModified: ZonedDateTime = ZonedDateTime.now())

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
        entries.map(entry => ServerRequests.put(s"/admin/blog/${entry.id}", entry.content, BasicAuthorization(user, password)))
      ) map ( _ => Map.empty )
    }
  }
}
