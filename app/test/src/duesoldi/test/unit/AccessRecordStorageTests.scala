package duesoldi.test.unit

import java.time.ZonedDateTime.now
import java.time.format.DateTimeFormatter.ISO_DATE_TIME

import duesoldi.metrics.storage.AccessRecordStore.Access
import duesoldi.metrics.storage.{AccessRecordStorage, GetAccessRecordArchive, GetAccessRecords}
import hammerspace.testing.CustomMatchers.EitherAssertions
import utest._

import scala.concurrent.Future

object AccessRecordStorageTests
extends TestSuite
{
  implicit val executionContext = utest.framework.ExecutionContext.RunNow
  val tests = this {
    "Getting all access records" - {
      "is empty if there are no access records or archives" - {
        val getNew: GetAccessRecords = _ => Future.successful(List.empty)
        val getArchives: GetAccessRecordArchive = _ => Future.successful(List.empty)
        val getAll = AccessRecordStorage.getIncludingArchived(getNew, getArchives)
        for {
          result <- getAll(now)
        } yield {
          assert(result == Right(Seq.empty))
        }
      }
      "contains new records since the start date" - {
        val getNew: GetAccessRecords = _ => Future.successful(List(simpleAccess))
        val getArchives: GetAccessRecordArchive = _ => Future.successful(List.empty)
        val getAll = AccessRecordStorage.getIncludingArchived(getNew, getArchives)
        for {
          result <- getAll(now.minusHours(1))
        } yield {
          assert(result isRightWhere(_.size == 1))
        }
      }
      "contains archived records since the start date" - {
        val getNew: GetAccessRecords = _ => Future.successful(List(simpleAccess))
        val getArchives: GetAccessRecordArchive = _ => Future.successful(List(
          (now().minusHours(1) -> now().minusMinutes(30),
            s"""Timestamp,Path,Referer,User-Agent,Duration (ms),Client IP,Country,Status Code,Request ID
              |"${now().minusMinutes(45).format(ISO_DATE_TIME)}","","","","0","","","200",""""".stripMargin)
        ))
        val getAll = AccessRecordStorage.getIncludingArchived(getNew, getArchives)
        for {
          result <- getAll(now.minusHours(1))
        } yield {
          assert(result isRightWhere(_.size == 2))
        }
      }
      "excludes archived records from before the start date" - {
        val getNew: GetAccessRecords = _ => Future.successful(List(simpleAccess))
        val getArchives: GetAccessRecordArchive = _ => Future.successful(List(
          (now().minusHours(2) -> now().minusMinutes(30),
            s"""Timestamp,Path,Referer,User-Agent,Duration (ms),Client IP,Country,Status Code,Request ID
              |"${now().minusMinutes(45).format(ISO_DATE_TIME)}","","","","0","","","200",""
              |"${now().minusMinutes(90).format(ISO_DATE_TIME)}","","","","0","","","200",""""".stripMargin)
        ))
        val getAll = AccessRecordStorage.getIncludingArchived(getNew, getArchives)
        for {
          result <- getAll(now.minusHours(1))
        } yield {
          assert(result isRightWhere(_.size == 2))
        }
      }
    }
  }

  private lazy val simpleAccess = Access(
    path = "",
    time = now(),
    referer = None,
    userAgent = None,
    duration = 0,
    clientIp = None,
    country = None,
    statusCode = 200,
    id = ""
  )

}
