import ReleaseTransformations._

organization       := "com.dripower"
name               := "play-circe"
scalaVersion       := "3.3.5"
crossScalaVersions := Seq("2.13.14", scalaVersion.value)

val playV  = "3.0.7"
val circeV = "0.14.12"

val crossDeps = Seq(
  "io.circe"          %% "circe-core"    % circeV,
  "io.circe"          %% "circe-parser"  % circeV,
  "io.circe"          %% "circe-generic" % circeV  % Test,
  "org.scalameta"     %% "munit"         % "1.0.1" % Test,
  "org.hamcrest"       % "hamcrest"      % "2.2"   % Test,
  "org.playframework" %% "play"          % playV   % Provided,
  "org.playframework" %% "play-guice"    % playV   % Provided
)

libraryDependencies ++= crossDeps

scalacOptions := {
  Seq(
    "-release:11",
    "-deprecation",
    "-encoding",
    "UTF-8",
    "-feature"
  )
}

// POM settings for Sonatype
homepage := Some(url("https://github.com/jilen/play-circe"))

scmInfo := Some(ScmInfo(url("https://github.com/jilen/play-circe"), "git@github.com:jilen/play-circe.git"))

developers += Developer("jilen", "jilen", "jilen.zhang@gmail.com", url("https://github.com/jilen"))

licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  setNextVersion,
  commitNextVersion,
  pushChanges
)
