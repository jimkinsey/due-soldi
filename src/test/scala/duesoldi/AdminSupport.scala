package duesoldi

object AdminSupport {

  def adminCredentials(username: String, password: String) = new SyncSetup {
    override def setup(env: Env) = Map("ADMIN_CREDENTIALS" -> s"$username:$password")
  }

}
