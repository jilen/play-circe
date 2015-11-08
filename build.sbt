organization := "play-circe"

name := "play-circe"

version := "0.2.0"

scalaVersion := "2.11.7"

crossScalaVersions := Seq("2.10.6", "2.11.7")

libraryDependencies ++= {
  val playV = "2.4.3"
  val circeV = "0.2.0"
  Seq(
    "io.circe" %% "circe-core" % circeV,
    "io.circe" %% "circe-generic" % circeV,
    "io.circe" %% "circe-parse" % circeV,
    "com.typesafe.play" %% "play" % playV % "provided",
    "org.scalatest" %% "scalatest" % "2.2.5" % "test",
    "com.typesafe.play" %% "play-test" % playV % "test",
    "com.typesafe.play" %% "play-ws" % playV % "test"
  )
}
