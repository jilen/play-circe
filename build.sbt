import ReleaseTransformations._

organization := "com.dripower"

name := "play-circe"

scalaVersion := "2.13.6"

crossScalaVersions := Seq("2.12.14", "2.13.6")

libraryDependencies ++= {
  val playV  = "2.8.8"
  val circeV = "0.13.0"
  Seq(
    "io.circe"               %% "circe-core"            % circeV,
    "io.circe"               %% "circe-parser"          % circeV,
    "com.typesafe.play"      %% "play"                  % playV   % Provided,
    "io.circe"               %% "circe-generic"         % circeV  % Test,
    "org.scalatestplus.play" %% "scalatestplus-play"    % "5.1.0" % Test,
    "com.typesafe.play"      %% "play-ws"               % playV   % Test,
    "com.typesafe.play"      %% "play-akka-http-server" % playV   % Test
  )
}

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-feature",
  "-unchecked",
  "-Xlint",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard"
)

// POM settings for Sonatype
homepage := Some(url("https://github.com/jilen/play-circe"))

scmInfo := Some(ScmInfo(url("https://github.com/jilen/play-circe"), "git@github.com:jilen/play-circe.git"))

developers += Developer("jilen", "jilen", "jilen.zhang@gmail.com", url("https://github.com/jilen"))

licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))

pomIncludeRepository := (_ => false)

// Add sonatype repository settings
publishTo := Some(
  if (isSnapshot.value)
    Opts.resolver.sonatypeSnapshots
  else
    Opts.resolver.sonatypeStaging
)

// release plugin

releaseCrossBuild := true

releasePublishArtifactsAction := PgpKeys.publishSigned.value // Use publishSigned in publishArtifacts step

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  publishArtifacts,
  setNextVersion,
  commitNextVersion,
  releaseStepCommand("sonatypeReleaseAll"),
  pushChanges
)
Global / useGpg := false

// COMMANDS ALIASES
addCommandAlias("t", "test")
addCommandAlias("to", "testOnly")
addCommandAlias("tq", "testQuick")
addCommandAlias("tsf", "testShowFailed")

addCommandAlias("c", "compile")
addCommandAlias("tc", "test:compile")

addCommandAlias("f", "scalafmt")             // Format production files according to ScalaFmt
addCommandAlias("fc", "scalafmtCheck")       // Check if production files are formatted according to ScalaFmt
addCommandAlias("tf", "test:scalafmt")       // Format test files according to ScalaFmt
addCommandAlias("tfc", "test:scalafmtCheck") // Check if test files are formatted according to ScalaFmt
addCommandAlias("fmt", ";f;tf")              // Format all files according to ScalaFmt

// All the needed tasks before pushing to the repository (compile, compile test, format check in prod and test)
addCommandAlias("prep", ";c;tc;test")
addCommandAlias("build", ";c;tc")
