name := "due-soldi"

organization := "com.github.jimkinsey"

version := "1.0"

scalaVersion := "2.12.1"

resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http-core" % "10.0.10",
  "com.typesafe.akka" %% "akka-http" % "10.0.10",
  "org.postgresql" % "postgresql" % "9.4.1212",
  "org.typelevel" %% "cats-core" % "1.0.0-MF",
  "com.vladsch.flexmark" % "flexmark" % "0.27.0",
  "com.github.jimkinsey" %% "bhuj" % "0.2-SNAPSHOT",
  "io.circe" %% "circe-yaml" % "0.7.0-M1",
  "com.h2database" % "h2" % "1.4.193" % "test",
  "net.databinder.dispatch" %% "dispatch-core" % "0.13.2" % "test",
  "org.jsoup" % "jsoup" % "1.10.3" % "test",
  "com.lihaoyi" %% "utest" % "0.5.4" % "test"
)

testFrameworks += new TestFramework("duesoldi.test.support.app.TestAppFramework")

cancelable in Global := true

enablePlugins(JavaAppPackaging)
