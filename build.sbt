organization := "play-circe"

name := "play-circe"

scalaVersion := "2.11.8"

crossScalaVersions := Seq("2.11.8")

libraryDependencies ++= {
  val playV = "2.5.6"
  val circeV = "0.5.1"
  Seq(
    "io.circe" %% "circe-core" % circeV,
    "io.circe" %% "circe-generic" % circeV,
    "io.circe" %% "circe-parser" % circeV,
    "com.typesafe.play" %% "play" % playV % "provided",
    "org.scalatest" %% "scalatest" % "2.2.5" % "test",
    "com.typesafe.play" %% "play-test" % playV % "test",
    "com.typesafe.play" %% "play-ws" % playV % "test"
  )
}


licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-unchecked",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture",
  "-Ywarn-unused-import")
