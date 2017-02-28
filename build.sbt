name := "due-soldi"

organization := "com.github.jimkinsey"

version := "1.0"

scalaVersion := "2.12.1"

resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http-core" % "10.0.1",
  "com.typesafe.akka" %% "akka-http" % "10.0.1",
  "org.scala-lang.modules" %% "scala-xml" % "1.0.5",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "net.databinder.dispatch" %% "dispatch-core" % "0.12.0" % "test",
  "com.h2database" % "h2" % "1.4.193",
  "org.postgresql" % "postgresql" % "9.4.1212",
  "org.jsoup" % "jsoup" % "1.10.2" % "test",
  "org.typelevel" %% "cats" % "0.9.0",
  "com.vladsch.flexmark" % "flexmark" % "0.11.1",
  "com.github.jimkinsey" %% "bhuj" % "0.2-SNAPSHOT"
)

cancelable in Global := true

enablePlugins(JavaAppPackaging)
