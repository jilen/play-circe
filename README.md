[Circe](https://github.com/travisbrown/circe) support for [playframework](https://playframework.com/)
=====================================================================================================
[![Build Status](https://travis-ci.org/jilen/play-circe.svg?branch=master)](https://travis-ci.org/jilen/play-circe)
[![Codacy Badge](https://api.codacy.com/project/badge/grade/c9fcd36a885546f8b9d2a427853e5353)](https://www.codacy.com/app/jilen-zhang/play-circe)
[![codecov.io](https://codecov.io/github/jilen/play-circe/coverage.svg?branch=master)](https://codecov.io/github/jilen/play-circe?branch=master)

How to get it
-------------
- Add repository

> resolvers += "Bintary JCenter" at "http://jcenter.bintray.com"

- Add dependency

For play 2.6.x

> libraryDependencies += "play-circe" %% "play-circe" % "2.6-0.8.0"


Usage
-----

```scala
package play.api.libs.circe

import io.circe.generic.auto._
import io.circe.syntax._
import play.api._
import play.api.mvc._

class CirceController(val controllerComponents: ControllerComponents) extends BaseController with Circe {

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

If you want to customize the json output, you can provide an implicit `Printer` in scope 
(default is `Printer.noSpaces`):

```scala
import io.circe.Printer

implicit val customPrinter = Printer.spaces2.copy(dropNullKeys = true)
```
