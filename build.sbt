organization := "scalax"

name := "play-circe"

version := "0.2.0"

scalaVersion := "2.11.7"

crossScalaVersions := Seq("2.10.6", "2.11.7")

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core" % "0.2.0",
  "io.circe" %% "circe-generic" % "0.2.0",
  "io.circe" %% "circe-parse" % "0.2.0",
  "com.typesafe.play" %% "play" % "2.4.3" % "provided",
  "org.scalatest" %% "scalatest" % "2.2.5" % "test",
  "com.typesafe.play" %% "play-test" % "2.4.3" % "test",
  "com.typesafe.play" %% "play-ws" % "2.4.3" % "test"
)
