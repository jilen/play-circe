import ReleaseTransformations._

organization := "com.dripower"

scalaVersion := "2.12.8"

crossScalaVersions := Seq("2.11.12", "2.12.8")

def playV = {
  sys.props.get("play.version").getOrElse("2.7.0")
}

def scalaTestPlusV = {
  if(playV.startsWith("2.6")) "3.1.2" else "4.0.0"
}

name := s"play-circe${playV.take(3).replace(".", "")}"

libraryDependencies ++= {
  val circeV = "0.11.0"
  Seq(
    "io.circe" %% "circe-core" % circeV,
    "io.circe" %% "circe-parser" % circeV,
    "com.typesafe.play" %% "play" % playV % Provided,
    "io.circe" %% "circe-generic" % circeV % Test,
    "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusV % Test,
    "com.typesafe.play" %% "play-ws" % playV % Test
  )
}

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

// POM settings for Sonatype
homepage := Some(url("https://github.com/jilen/play-circe"))

scmInfo := Some(ScmInfo(url("https://github.com/jilen/play-circe"),
  "git@github.com:jilen/play-circe.git"))

developers += Developer("jilen",
  "jilen",
  "jilen.zhang@gmail.com",
  url("https://github.com/jilen"))

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
