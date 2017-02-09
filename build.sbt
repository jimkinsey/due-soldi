name := "due-soldi"

organization := "com.github.jimkinsey"

version := "1.0"

scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http-core" % "10.0.1",
  "com.typesafe.akka" %% "akka-http" % "10.0.1",
  "org.scala-lang.modules" %% "scala-xml" % "1.0.5",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "net.databinder.dispatch" %% "dispatch-core" % "0.12.0" % "test",
  "org.jsoup" % "jsoup" % "1.10.2" % "test",
  "org.typelevel" %% "cats" % "0.9.0",
  "com.vladsch.flexmark" % "flexmark" % "0.11.1"
)

cancelable in Global := true

enablePlugins(JavaAppPackaging)
