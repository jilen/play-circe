package play.api.libs.circe

import io.circe.generic.auto._
import io.circe.parse._
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import play.api.libs.ws._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.circe.Conf._
import scala.concurrent.Future
import scala.concurrent.duration._

class CirceSpec extends FlatSpec with ShouldMatchers with EitherValues with ScalaFutures with BeforeAndAfter {

  implicit override val patienceConfig =
  PatienceConfig(timeout = 10.seconds)

  before {
    println("staring test server")
    server.start()
  }

  after {
    println("stoping testing server")
    server.stop()
  }

  "Circe" should "server json" in {
    val result = WS.url(s"http://127.0.0.1:${Conf.port}/get").get().map(_.body)
    decode[Foo](result.futureValue).toEither.right.value shouldEqual(Conf.foo)
  }
}
