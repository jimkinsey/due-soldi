import mill._
import mill.scalalib._
import coursier.maven.MavenRepository

object core extends ScalaModule {
  def scalaVersion = "2.12.4"
  def repositories = super.repositories ++ Seq(
    MavenRepository("https://oss.sonatype.org/content/repositories/releases"),
    MavenRepository("https://oss.sonatype.org/content/repositories/snapshots")
  )
  def ivyDeps = Agg(
    ivy"org.postgresql:postgresql:9.4.1212",
    ivy"com.vladsch.flexmark:flexmark:0.27.0",
    ivy"com.github.jimkinsey::bhuj:0.2-SNAPSHOT",
<<<<<<< 91ca2762ffb85b4f0925a0af496d15c8450c39af
  )
  object test extends Tests{
    def ivyDeps = Agg(
      ivy"com.h2database:h2:1.4.193",
      ivy"net.databinder.dispatch::dispatch-core:0.13.2",
      ivy"org.jsoup:jsoup:1.10.3",
      ivy"com.lihaoyi::utest:0.6.0"
    )
    def testFrameworks = Seq("duesoldi.test.support.app.TestAppFramework")
  }
}

=======
    ivy"com.h2database:h2:1.4.193",
    ivy"net.databinder.dispatch::dispatch-core:0.13.2",
    ivy"org.jsoup:jsoup:1.10.3",
    ivy"com.lihaoyi::utest:0.5.4"
  )
  object test extends Tests{
    def ivyDeps = Agg(
      ivy"com.lihaoyi::utest:0.6.0",
      ivy"com.lihaoyi:mill-bridge_2.12.4:0.1" // FIXME
    )
    def testFramework = "duesoldi.test.support.app.TestAppFramework"
  }
}
>>>>>>> Initial investigation of using Mill to build the app
