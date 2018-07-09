package duesoldi.test.integration

import java.time.ZonedDateTime.now

import duesoldi.Env
import duesoldi.metrics.storage.AccessRecordArchiveStore
import duesoldi.test.support.setup.{Database, Setup}
import hammerspace.storage.JDBCConnection
import hammerspace.storage.JDBCConnection.OpenConnection
import utest._

object AccessRecordArchiveStoreTests
extends TestSuite
{
  implicit val executionContext = utest.framework.ExecutionContext.RunNow
  val tests = this
  {
    "get" - {
      "returns no archives when there is no archive with a range covering the provided date" - {
        Setup.withSetup(
          Database.database
        ) { env =>
          val open = openConnection(env)
          val performQuery = JDBCConnection.performQuery(open, prepare, executeQuery)(AccessRecordArchiveStore.toArchive)
          for {
            archive <- AccessRecordArchiveStore.get(performQuery)(now().plusHours(1))
          } yield {
            assert(archive isEmpty)
          }
        }
      }
      "returns archives when there are archives with a range covering the provided date" - {
        Setup.withSetup(
          Database.database
        ) { env =>
          val open = openConnection(env)
          val performQuery = JDBCConnection.performQuery(open, prepare, executeQuery)(AccessRecordArchiveStore.toArchive)
          val performUpdate = JDBCConnection.performUpdate(open, prepare, executeUpdate)

          val twoHoursAgo = now().minusHours(2)
          val oneHourAgo = now().minusHours(1)
          val thirtyMinutesAgo = now().minusMinutes(30)

          for {
            _ <- AccessRecordArchiveStore.put(performUpdate)((twoHoursAgo, oneHourAgo), "archive1")
            _ <- AccessRecordArchiveStore.put(performUpdate)((oneHourAgo, thirtyMinutesAgo), "archive2")
            archive <- AccessRecordArchiveStore.get(performQuery)(now().minusMinutes(90))
          } yield {
            assert(archive.size == 2)
          }
        }
      }
    }
    "delete" - {
      "deletes the archive when it exists" - {
        Setup.withSetup(
          Database.database
        ) { env =>
          val open = openConnection(env)
          val performQuery = JDBCConnection.performQuery(open, prepare, executeQuery)(AccessRecordArchiveStore.toArchive)
          val performUpdate = JDBCConnection.performUpdate(open, prepare, executeUpdate)

          for {
            _ <- AccessRecordArchiveStore.put(performUpdate)((now().minusHours(2), now().minusHours(1)), "archive1")
            _ <- AccessRecordArchiveStore.put(performUpdate)((now().minusHours(1), now().minusHours(0)), "archive2")
            preDeletion <- AccessRecordArchiveStore.get(performQuery)(now().minusMinutes(90))
            toDelete = preDeletion.head
            _ <- AccessRecordArchiveStore.delete(performUpdate)(toDelete)
            postDeletion <- AccessRecordArchiveStore.get(performQuery)(now().minusMinutes(90))
          } yield {
            assert(postDeletion.size == 1)
          }
        }
      }
    }
  }

  private lazy val openConnection: Env => OpenConnection = (env) => {
    val details = JDBCConnection.ConnectionDetails(
      url = env("JDBC_DATABASE_URL"),
      username = env("JDBC_DATABASE_USERNAME"),
      password = env("JDBC_DATABASE_PASSWORD")
    )
    JDBCConnection.openConnection(details)
  }

  private lazy val prepare = JDBCConnection.prepareStatement _
  private lazy val executeQuery = JDBCConnection.executeQuery _
  private lazy val executeUpdate = JDBCConnection.executeUpdate _

}
