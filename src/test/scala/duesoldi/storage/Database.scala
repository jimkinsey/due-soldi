package duesoldi.storage

import java.io.InputStreamReader
import java.sql.Connection
import java.util.UUID
import javax.sql.DataSource

import duesoldi.{ScriptRunner, Setup, _}
import org.h2.jdbcx.JdbcConnectionPool

import scala.concurrent.{ExecutionContext, Future}

trait Database {

  implicit def executionContext: ExecutionContext

  lazy val noDatabase = new Setup {
    override def setup(env: Env): Future[Env] = Future.successful(Map.empty)
    override def tearDown: Future[Unit] = Future.successful({})
  }

  def database = new Setup {
    private val id = UUID.randomUUID().toString.take(8)
    private var connection: Connection = _

    override def setup(env: Env): Future[Env] = Future {
      val ds: DataSource = JdbcConnectionPool.create(s"jdbc:h2:mem:test-$id;DB_CLOSE_DELAY=-1", "user", "password")
      connection = ds.getConnection()
      new ScriptRunner(connection, true, true).runScript(new InputStreamReader(AccessRecordStore.getClass.getResourceAsStream("/database/init.sql")))
      Map(
        "JDBC_DATABASE_URL" -> connection.getMetaData.getURL,
        "JDBC_DATABASE_USERNAME" -> "user",
        "JDBC_DATABASE_PASSWORD" -> "password"
      )
    }

    override def tearDown: Future[Unit] = {
      Future.successful(connection.close())
    }

  }

}
