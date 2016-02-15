package play.api.libs.circe

import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import play.api.libs.ws._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.circe.Conf._
import scala.concurrent.Future
import scala.concurrent.duration._

class CirceSpec extends FlatSpec
    with ShouldMatchers
    with EitherValues
    with ScalaFutures
    with BeforeAndAfter{

  val t = DurationInt(10).seconds
  override implicit  val patienceConfig =
  PatienceConfig(timeout = t)

  before {
    println("staring test server")
    server.start()
  }

  after {
    println("stoping testing server")
    server.stop()
  }

  val serverUrl = s"http://127.0.0.1:${Conf.port}"

  "Circe" should "server json" in {
    val result = WS.url(s"$serverUrl/get").get().map(_.body)
    decode[Foo](result.futureValue).toEither.right.value shouldEqual(Conf.foo)
  }

  it should "parse json to model" in {
    val result = WS.url(s"$serverUrl/post")
      .withHeaders("Content-Type" -> "application/json")
      .post(Conf.foo.asJson.noSpaces).map(_.body)
    result.futureValue shouldBe "true"
  }

  it should "return bad status while parsing non-json request" in {
    val result = WS.url(s"$serverUrl/post")
      .post(Conf.foo.asJson.noSpaces).map(_.status)
    result.futureValue should not be 200
  }

  it should "parse json" in {
    val result = WS.url(s"$serverUrl/post-json")
      .withHeaders("Content-Type" -> "application/json")
      .post(Conf.foo.asJson.noSpaces).map(_.body)
    result.futureValue shouldBe "true"
  }

  it should "parse tolerant json to model" in {
    val result = WS.url(s"$serverUrl/post-tolerant")
      .post(Conf.foo.asJson.noSpaces).map(_.body)
    result.futureValue shouldBe "true"
  }

  it should "parse tolerant json" in {
    val result = WS.url(s"$serverUrl/post-tolerant-json")
      .post(Conf.foo.asJson.noSpaces).map(_.body)
    result.futureValue shouldBe "true"
  }
}
