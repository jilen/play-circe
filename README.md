[Circe](https://github.com/travisbrown/circe) support for [playframework](https://playframework.com/)
=====================================================================================================
[![Build Status](https://travis-ci.org/jilen/play-circe.svg?branch=2.4_x)](https://travis-ci.org/jilen/play-circe)
[![Codacy Badge](https://api.codacy.com/project/badge/grade/c9fcd36a885546f8b9d2a427853e5353)](https://www.codacy.com/app/jilen-zhang/play-circe)
[![codecov.io](https://codecov.io/github/jilen/play-circe/coverage.svg?branch=2.4_x)](https://codecov.io/github/jilen/play-circe?branch=master)

How to get it
-------------
- Add repository

> resolvers += "Bintary JCenter" at "http://jcenter.bintray.com"

- Add dependency

For play 2.5.x

> libraryDependencies += "play-circe" %% "play-circe" % "2.5-0.6.0"

For play 2.4.x

> libraryDependencies += "play-circe" %% "play-circe" % "2.4-0.6.0"



Usage
-----

```scala
package play.api.libs.circe

import io.circe.generic.auto._
import io.circe.syntax._
import play.api._
import play.api.mvc._

object CirceController extends Controller with Circe {

  case class Bar(bar: Int)
  case class Foo(foo: String, bar: Bar)

  val bar = Bar(1)
  val foo = Foo("foo", bar)

  //serve json
  def get = Action {
    Ok(foo.asJson)
  }

  //parse json to case class
  def post = Action(circe.json[Foo]) { implicit request =>
    val isEqual = request.body == foo
    Ok(isEqual.toString)
  }

  def postJson = Action(circe.json) { implicit request =>
    val isEqual = request.body == foo.asJson
    Ok(isEqual.toString)
  }

  def postTolerate = Action(circe.tolerantJson[Foo]) { implicit request =>
    val isEqual = request.body == foo
    Ok(isEqual.toString)
  }

  def postTolerateJson = Action(circe.tolerantJson) { implicit request =>
    val isEqual = request.body == foo.asJson
    Ok(isEqual.toString)
  }
}
```
