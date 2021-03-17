package duesoldi.dependencies

import duesoldi.dependencies.Injection.Inject
import hammerspace.storage.JDBCConnection
import hammerspace.storage.JDBCConnection.{ConnectionDetails, PerformQuery, PerformUpdate}

import java.sql.ResultSet

trait JDBCDependencies {

  implicit val jdbcConnectionDetails: Inject[ConnectionDetails] = _.jdbcConnectionDetails

  implicit val jdbcPerformUpdate: Inject[PerformUpdate] = { config =>
    JDBCConnection.performUpdate(
      JDBCConnection.openConnection(config.jdbcConnectionDetails),
      JDBCConnection.prepareStatement,
      JDBCConnection.executeUpdate
    )
  }

  implicit def jdbcPerformQuery[T](implicit translate: ResultSet => T): Inject[PerformQuery[T]] = { config =>
    JDBCConnection.performQuery(
      JDBCConnection.openConnection(config.jdbcConnectionDetails),
      JDBCConnection.prepareStatement,
      JDBCConnection.executeQuery
    )
  }
}
