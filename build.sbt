name := "due-soldi"

organization := "com.github.jimkinsey"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http-core" % "10.0.1",
  "com.typesafe.akka" %% "akka-http" % "10.0.1",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
   "net.databinder.dispatch" %% "dispatch-core" % "0.11.2" % "test"
)

cancelable in Global := true