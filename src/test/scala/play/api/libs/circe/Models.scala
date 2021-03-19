package play.api.libs.circe

case class Bar(bar: Int)
case class Foo(foo: String, bar: Bar)

object Data {
  val bar = Bar(1)
  val foo = Foo("foo", bar)
}
