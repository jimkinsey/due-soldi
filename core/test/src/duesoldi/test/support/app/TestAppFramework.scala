package duesoldi.test.support.app

import scala.concurrent.ExecutionContext

class TestAppFramework extends utest.runner.Framework
{
  implicit val executionContext: ExecutionContext = utest.framework.ExecutionContext.RunNow
  override def setup(): Unit = {
    println("Starting test app...")
    TestApp.attemptStart
  }
  override def teardown(): Unit = {
    TestApp.attemptStop
  }
}