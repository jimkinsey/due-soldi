package duesoldi.test.integration

import java.time.ZonedDateTime

import duesoldi.metrics.storage.AccessRecordArchiveStore
import duesoldi.test.support.setup.{Database, Setup}
import hammerspace.storage.JDBCConnection
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
          val details = JDBCConnection.ConnectionDetails(
            url = env("JDBC_DATABASE_URL"),
            username = env("JDBC_DATABASE_USERNAME"),
            password = env("JDBC_DATABASE_PASSWORD")
          )
          val open = JDBCConnection.openConnection(details)
          val prepare = JDBCConnection.prepareStatement(_)
          val execute = JDBCConnection.executeQuery(_)
          val performQuery = JDBCConnection.performQuery(open, prepare, execute)(AccessRecordArchiveStore.toArchive)
          for {
            archive <- AccessRecordArchiveStore.get(performQuery)(ZonedDateTime.now().plusHours(1))
          } yield {
            assert(archive isEmpty)
          }
        }
      }
      "returns archives when there are archives with a range covering the provided date" - {
        Setup.withSetup(
          Database.database
        ) { env =>
          val details = JDBCConnection.ConnectionDetails(
            url = env("JDBC_DATABASE_URL"),
            username = env("JDBC_DATABASE_USERNAME"),
            password = env("JDBC_DATABASE_PASSWORD")
          )
          val open = JDBCConnection.openConnection(details)
          val prepare = JDBCConnection.prepareStatement(_)
          val execute = JDBCConnection.executeQuery(_)
          val executeUpdate = JDBCConnection.executeUpdate(_)
          val performQuery = JDBCConnection.performQuery(open, prepare, execute)(AccessRecordArchiveStore.toArchive)
          val performUpdate = JDBCConnection.performUpdate(open, prepare, executeUpdate)

          val twoHoursAgo = ZonedDateTime.now().minusHours(2)
          val oneHourAgo = ZonedDateTime.now().minusHours(1)
          val thirtyMinutesAgo = ZonedDateTime.now().minusMinutes(30)

          for {
            _ <- AccessRecordArchiveStore.put(performUpdate)((twoHoursAgo, oneHourAgo), "archive1")
            _ <- AccessRecordArchiveStore.put(performUpdate)((oneHourAgo, thirtyMinutesAgo), "archive2")
            archive <- AccessRecordArchiveStore.get(performQuery)(ZonedDateTime.now().minusMinutes(90))
          } yield {
            assert(archive.size == 2)
          }
        }
      }
    }
  }
}
