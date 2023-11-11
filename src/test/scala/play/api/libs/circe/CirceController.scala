package play.api.libs.circe

import org.apache.pekko.actor._
import io.circe.{Printer, Json}
import io.circe.syntax._
import play.api.mvc._

import scala.concurrent.ExecutionContextExecutor

class CirceController(val controllerComponents: ControllerComponents) extends BaseController with Circe {

  implicit val customPrinter: Printer = Printer.spaces2.copy(dropNullValues = true)

  def get: Action[AnyContent] = Action {
    Ok(Data.foo.asJson)
  }

  def post = Action(circe.json[Foo]) { (request: Request[Foo]) =>
    val isEqual = request.body == Data.foo
    Ok(isEqual.toString)
  }

  def postJson = Action(circe.json) { (request: Request[Json]) =>
    val isEqual = request.body == Data.foo.asJson
    Ok(isEqual.toString)
  }

  def postTolerant = Action(circe.tolerantJson[Foo]) { (request: Request[Foo]) =>
    val isEqual = request.body == Data.foo
    Ok(isEqual.toString)
  }

  def postTolerantJson = Action(circe.tolerantJson) { (request: Request[Json]) =>
    val isEqual = request.body == Data.foo.asJson
    Ok(isEqual.toString)
  }
}

object CirceController {
  implicit val actorSystem: ActorSystem     = ActorSystem()
  implicit val ec: ExecutionContextExecutor = actorSystem.dispatcher
}
