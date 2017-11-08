package duesoldi.test.support.setup

import java.io.InputStreamReader
import java.sql.Connection
import java.util.UUID
import javax.sql.DataSource

import duesoldi._
import duesoldi.storage.AccessRecordStore
import duesoldi.test.support.database.ScriptRunner
import org.h2.jdbcx.JdbcConnectionPool

object Database
{
  lazy val noDatabase = new SyncSetup {
    override def setup(env: Env) = Map.empty
    override def tearDown = {}
  }

  def database = new SyncSetup {
    private val id = UUID.randomUUID().toString.take(8)
    private var connection: Connection = _

    override def setup(env: Env) = {
      val ds: DataSource = JdbcConnectionPool.create(s"jdbc:h2:mem:test-$id;DB_CLOSE_DELAY=-1", "user", "password")
      connection = ds.getConnection()
      new ScriptRunner(connection, true, true).runScript(new InputStreamReader(AccessRecordStore.getClass.getResourceAsStream("/database/init.sql")))
      Map(
        "JDBC_DATABASE_URL" -> connection.getMetaData.getURL,
        "JDBC_DATABASE_USERNAME" -> "user",
        "JDBC_DATABASE_PASSWORD" -> "password"
      )
    }

    override def tearDown = {
      connection.close()
    }
  }
}