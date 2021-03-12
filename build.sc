import mill._
import mill.scalalib._
import coursier.maven.MavenRepository

object hammerspace extends ScalaModule {
  def scalaVersion = "2.12.4"
  def ivyDeps = Agg(
    ivy"com.vladsch.flexmark:flexmark:0.27.0"
  )
  object test extends Tests{
    def ivyDeps = Agg(
      ivy"com.lihaoyi::utest:0.6.0"
    )
    def testFrameworks = Seq("utest.runner.Framework")
  }
}

object ratatoskr extends ScalaModule {
  def scalaVersion = "2.12.4"
  def moduleDeps = Seq(hammerspace)
  object test extends Tests{
    def ivyDeps = Agg(
      ivy"com.lihaoyi::utest:0.6.0"
    )
    def testFrameworks = Seq("utest.runner.Framework")
  }
}

object cicerone extends ScalaModule {
  def scalaVersion = "2.12.4"
  def moduleDeps = Seq(hammerspace, ratatoskr)
  object test extends Tests{
    def ivyDeps = Agg(
      ivy"com.lihaoyi::utest:0.6.0"
    )
    def testFrameworks = Seq("utest.runner.Framework")
  }
}

object dearboy extends ScalaModule {
  def scalaVersion = "2.12.4"
  object test extends Tests{
    def ivyDeps = Agg(
      ivy"com.lihaoyi::utest:0.6.0"
    )
    def testFrameworks = Seq("utest.runner.Framework")
  }
}

object sommelier extends ScalaModule {
  def scalaVersion = "2.12.4"
  def moduleDeps = Seq(hammerspace, cicerone, dearboy)
  object test extends Tests{
    def ivyDeps = Agg(
      ivy"com.lihaoyi::utest:0.6.0"
    )
    def testFrameworks = Seq("utest.runner.Framework")
  }
}

object app extends ScalaModule {
  def scalaVersion = "2.12.4"
  def repositories = super.repositories ++ Seq(
    MavenRepository("https://oss.sonatype.org/content/repositories/releases"),
    MavenRepository("https://oss.sonatype.org/content/repositories/snapshots")
  )
  def moduleDeps = Seq(hammerspace, cicerone, dearboy, sommelier)
  def ivyDeps = Agg(
    ivy"org.postgresql:postgresql:9.4.1212",
    ivy"com.github.jimkinsey::bhuj:0.2-SNAPSHOT",
    ivy"com.amazonaws:aws-java-sdk-s3:1.11.327"
  )
  object test extends Tests{
    def ivyDeps = Agg(
      ivy"com.h2database:h2:1.4.193",
      ivy"org.jsoup:jsoup:1.10.3",
      ivy"com.lihaoyi::utest:0.6.0"
    )
    def testFrameworks = Seq("duesoldi.test.support.app.TestAppFramework")
  }
}
