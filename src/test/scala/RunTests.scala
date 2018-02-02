import utest._

object RunTests {

  def main(args: Array[String]): Unit = {
    println("Running tests!")
    val results = TestRunner.runAndPrint(duesoldi.test.unit.BlogEntrySerialisationTests.tests, "Test Suite")
    val (summary, successes, failures) = TestRunner.renderResults(
      Seq(
        "Test Suite" -> results,
      )
    )
    if (failures > 0) sys.exit(1)
  }

}
