organization := "play-circe"

name := "play-circe"

scalaVersion := "2.11.11"

crossScalaVersions := Seq("2.11.11", "2.12.2")

libraryDependencies ++= {
  val playV = "2.6.2"
  val circeV = "0.8.0"
  Seq(
    "io.circe" %% "circe-core" % circeV,
    "io.circe" %% "circe-generic" % circeV,
    "io.circe" %% "circe-parser" % circeV,
    "com.typesafe.play" %% "play" % playV % Provided,
    "org.scalatestplus.play" %% "scalatestplus-play" % "3.0.0" % Test,
    "com.typesafe.play" %% "play-ws" % playV % Test
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
