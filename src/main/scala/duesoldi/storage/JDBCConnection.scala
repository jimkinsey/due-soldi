package duesoldi.storage

import java.sql.{Connection, DriverManager, ResultSet}

import scala.util.Try

trait JDBCConnection {

  def url: String
  def username: String
  def password: String

  def withConnection[T](block: Connection => T): Try[T] = {
    Try(DriverManager.getConnection(url, username, password)).flatMap { connection =>
      val res = Try(block(connection))
      connection.close()
      res
    }
  }

  def queryResults(query: String)(implicit connection: Connection): Stream[ResultSet] = {
    resultStream(connection.createStatement().executeQuery(query))
  }

  def resultStream(resultSet: ResultSet): Stream[ResultSet] = {
    resultSet.next() match {
      case false => Stream.empty
      case true  => resultSet #:: resultStream(resultSet)
    }
  }

}
