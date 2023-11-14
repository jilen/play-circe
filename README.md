[Circe](https://github.com/travisbrown/circe) support for [playframework](https://playframework.com/)
=====================================================================================================
[![Build](https://github.com/jilen/play-circe/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/jilen/play-circe/actions/workflows/build.yml)
[![codecov](https://codecov.io/gh/jilen/play-circe/branch/master/graph/badge.svg?token=SdVZrdCrzT)](https://codecov.io/gh/jilen/play-circe)

How to get it
-------------

- Add dependency

For play 2.6.x

> libraryDependencies += "com.dripower" %% "play-circe" % "2612.0"

For play 2.7.x

> libraryDependencies += "com.dripower" %% "play-circe" % "2712.0"

For play 2.8.x

> libraryDependencies += "com.dripower" %% "play-circe" % "2814.1"

For play 3.0.x

> libraryDependencies += "com.dripower" %% "play-circe" % "3014.1"


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

+ The `Circe` totally ignores the configured `HttpErrorHandler` and just uses `DefaultHttpErrorHandler`.
If this not what you want, simply make a trait to override `circeErrorHandler` like this
```scala
class MyController @Inject() (val errorHandler: HttpErrorHandler, val controllerComponents: ControllerComponents) extends BaseController with Circe {
  override def circeErrorHandler = errorHandler
}
```
