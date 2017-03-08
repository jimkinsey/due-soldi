package duesoldi.storage

import java.sql.{Connection, DriverManager, ResultSet}

import duesoldi.storage.JDBCConnection.ConnectionDetails

import scala.util.Try

object JDBCConnection {
  case class ConnectionDetails(url: String, username: String, password: String)
}

trait JDBCConnection {

  def connectionDetails: ConnectionDetails

  def withConnection[T](block: Connection => T): Try[T] = {
    Try(DriverManager.getConnection(url, username, password)).flatMap { connection =>
      val res = Try(block(connection))
      connection.close()
      res
    }
  }

  def queryResults(query: String, params: Any*)(implicit connection: Connection): Stream[ResultSet] = {
    val statement = connection.prepareStatement(query)
    params.zipWithIndex.foreach { case (param, index) =>
      statement.setObject(index + 1, param)
    }
    resultStream(statement.executeQuery())
  }

  def resultStream(resultSet: ResultSet): Stream[ResultSet] = {
    resultSet.next() match {
      case false => Stream.empty
      case true  => resultSet #:: resultStream(resultSet)
    }
  }

  private lazy val ConnectionDetails(url, username, password) = connectionDetails

}
