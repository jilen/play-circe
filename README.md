[Circe](https://github.com/travisbrown/circe) support for [playframework](https://playframework.com/)
=====================================================================================================
[![Build Status](https://travis-ci.org/jilen/play-circe.svg?branch=master)](https://travis-ci.org/jilen/play-circe)
[![Codacy Badge](https://api.codacy.com/project/badge/grade/c9fcd36a885546f8b9d2a427853e5353)](https://www.codacy.com/app/jilen-zhang/play-circe)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/c9fcd36a885546f8b9d2a427853e5353)](https://www.codacy.com/app/jilen-zhang/play-circe?utm_source=github.com&utm_medium=referral&utm_content=jilen/play-circe&utm_campaign=Badge_Coverage)

How to get it
-------------

- Add dependency

For play 2.6.x

> libraryDependencies += "com.dripower" %% "play-circe" % "2610.0"


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

# FAQ

+ If you want to customize the json output, you can provide an implicit `Printer` in scope 
(default is `Printer.noSpaces`):

```scala
import io.circe.Printer

implicit val customPrinter = Printer.spaces2.copy(dropNullValues = true)
```

+ The `Circe` totally ignore the configured `HttpErrorHandler` and just use `DefaultHttpErrorHandler`.
If this not what you want, simply make an trait to override `circeErrorHandler` like this
```scala
class MyController @Inject() (val errorHandler: HttpErrorHandler, val controllerComponents: ControllerComponents) extends BaseController with Circe {
  override def circeErrorHandler = errorHandler
}
```
