package play.api.libs.circe

import io.circe.{Encoder, Decoder}
import io.circe.generic.semiauto._

case class Bar(bar: Int)
object Bar {
  implicit val barEncoder: Encoder[Bar] = deriveEncoder[Bar]
  implicit val barDecoder: Decoder[Bar] = deriveDecoder[Bar]
}
case class Foo(foo: String, bar: Bar)
object Foo {
  implicit val fooEncoder: Encoder[Foo] = deriveEncoder[Foo]
  implicit val fooDecoder: Decoder[Foo] = deriveDecoder[Foo]
}

object Data {
  val bar = Bar(1)
  val foo = Foo("foo", bar)
}
