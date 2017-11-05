package duesoldi.storage

import java.sql.{Connection, DriverManager, PreparedStatement, ResultSet}

import duesoldi.exceptions._

import scala.util.Try

object JDBCConnection
{
  case class ConnectionDetails(url: String, username: String, password: String)

  type ExecuteQuery = (PreparedStatement) => Try[Stream[ResultSet]]
  type ExecuteUpdate = (PreparedStatement) => Try[Int]
  type PrepareStatement = (Connection) => (String, Seq[Any]) => Try[PreparedStatement]
  type OpenConnection = () => Try[Connection]
  type PerformQuery[T] = (String, Seq[Any]) => Try[List[T]]
  type PerformUpdate = (String, Seq[Any]) => Try[Int]

  def openConnection(details: ConnectionDetails): OpenConnection = () => {
    Try(DriverManager.getConnection(details.url, details.username, details.password))
  }

  def performUpdate(open: OpenConnection, prepare: PrepareStatement, execute: ExecuteUpdate): PerformUpdate = {
    (sql, vars) =>
      open().flatMap { connection =>
        val results = for {
          statement <- prepare(connection)(sql, vars)
          count <- execute(statement)
        } yield {
          count
        }
        results.fin({ Try(connection.close()) })
      }
  }

  def performQuery[T](open: OpenConnection, prepare: PrepareStatement, execute: ExecuteQuery)
                     (implicit translate: ResultSet => T) : PerformQuery[T] = {
    (sql, vars) =>
      open().flatMap { connection =>
        val results = for {
          statement <- prepare(connection)(sql, vars)
          resultSets <- execute(statement)
          translated <- Try(resultSets.map(translate))
        } yield {
          translated.toList
        }
        results.fin({ Try(connection.close()) })
      }
  }

  def prepareStatement(connection: Connection)(query: String, params: Seq[Any]): Try[PreparedStatement] = {
    Try {
      val statement = connection.prepareStatement(query)
      params.zipWithIndex.foreach { case (param, index) =>
        statement.setObject(index + 1, param)
      }
      statement
    }
  }

  def executeQuery(statement: PreparedStatement): Try[Stream[ResultSet]] = {
    Try(resultStream(statement.executeQuery()))
  }

  def executeUpdate(statement: PreparedStatement): Try[Int] = {
    Try(statement.executeUpdate())
  }

  def resultStream(resultSet: ResultSet): Stream[ResultSet] = {
    resultSet.next() match {
      case false => Stream.empty
      case true  => resultSet #:: resultStream(resultSet)
    }
  }
}
