package duesoldi

import scala.concurrent.Future

object AdminSupport {

  def adminCredentials(username: String, password: String) = new Setup {
    override def setup(env: Env): Future[Env] = Future successful Map("ADMIN_CREDENTIALS" -> s"$username:$password")
  }

}
