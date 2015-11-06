package play.api.libs.circe

case class Bar(bar: Int)
case class Foo(foo: String, bar: Bar)

import play.api.test._
import play.api.test.Helpers._

object Conf {

  val bar = Bar(1)
  val foo = Foo("foo", bar)
  val port = 12345

  implicit lazy val app = FakeApplication(
    withRoutes = {
      case ("GET", "/get") => CirceController.get
      case ("POST", "/post") => CirceController.post
    }
  )
  lazy val server =  new TestServer(port, app)
}
