package duesoldi.metrics.dependencies

import duesoldi.dependencies.Injection.{Inject, inject}
import duesoldi.dependencies.{JDBCDependencies, SessionCookieDependencies}
import duesoldi.metrics.storage.AccessRecordStore.Access
import duesoldi.metrics.storage._

import scala.concurrent.ExecutionContext

trait AccessRecordingDependencies
  extends JDBCDependencies
    with SessionCookieDependencies {

  implicit lazy val getAccessRecordsWithCount: Inject[GetAccessRecordsWithCount] = { config =>
    AccessRecordStore.getAllWithCount(jdbcPerformQuery[Access](AccessRecordStore.toAccess)(config))
  }

  implicit lazy val getAccessRecordLogSize: Inject[GetAccessRecordLogSize] = { config =>
    AccessRecordStore.getLogSize(jdbcPerformQuery[Access](AccessRecordStore.toAccess)(config))
  }

  implicit lazy val deleteAccessRecord: Inject[DeleteAccessRecord] = inject(AccessRecordStore.delete _)

  implicit lazy val updateAccessRecord: Inject[UpdateAccessRecord] = inject(AccessRecordStore.update _)

  implicit lazy val getAccessRecords: Inject[GetAccessRecords] = { config =>
    AccessRecordStore.getAll(jdbcPerformQuery[Access](AccessRecordStore.toAccess)(config))
  }

  implicit lazy val storeAccessRecord: Inject[StoreAccessRecord] = inject(AccessRecordStore.put _)

  implicit lazy val storeAccessRecordArchive: Inject[StoreAccessRecordArchive] = inject(AccessRecordArchiveStore.put _)

  implicit lazy val getAccessRecordArchive: Inject[GetAccessRecordArchive] = config => {
    AccessRecordArchiveStore.get(jdbcPerformQuery(AccessRecordArchiveStore.toArchive)(config))
  }

  implicit lazy val deleteAccessRecordArchive: Inject[DeleteAccessRecordArchive] = inject(AccessRecordArchiveStore.delete _)

  implicit def getAllAccessRecords(implicit executionContext: ExecutionContext): Inject[GetAllAccessRecords] = config => {
    AccessRecordStorage.getIncludingArchived(getAccessRecords(config), getAccessRecordArchive(config))
  }
}
