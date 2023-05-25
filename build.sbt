import ReleaseTransformations._

organization := "com.dripower"
name         := "play-circe"
scalaVersion := "2.13.10"

crossScalaVersions := Seq("2.12.17", "2.13.10", "3.3.0")

val playV  = "2.8.19"
val circeV = "0.14.5"

val crossDeps = Seq(
  "io.circe" %% "circe-core"    % circeV,
  "io.circe" %% "circe-parser"  % circeV,
  "io.circe" %% "circe-generic" % circeV % Test
)

val scala2Deps = Seq(
  "com.typesafe.play" %% "play" % playV % Provided,
  "com.typesafe.play" %% "play-guice" % playV % Provided
).map(_.cross(CrossVersion.for3Use2_13))

libraryDependencies ++= (crossDeps ++ scala2Deps)

scalacOptions := {
  Seq(
    "-deprecation",
    "-encoding",
    "UTF-8",
    "-feature",
    "-Xlint"
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
