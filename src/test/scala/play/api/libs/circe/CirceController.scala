package play.api.libs.circe

import akka.actor._
import io.circe.Printer
import io.circe.generic.auto._
import io.circe.syntax._
import play.api.mvc._

import scala.concurrent.ExecutionContextExecutor

class CirceController(val controllerComponents: ControllerComponents) extends BaseController with Circe {

  implicit val customPrinter: Printer = Printer.spaces2.copy(dropNullValues = true)

  def get: Action[AnyContent] = Action {
    Ok(Data.foo.asJson)
  }

  def post = Action(circe.json[Foo]) { implicit request =>
    val isEqual = request.body == Data.foo
    Ok(isEqual.toString)
  }

  def postJson = Action(circe.json) { implicit request =>
    val isEqual = request.body == Data.foo.asJson
    Ok(isEqual.toString)
  }

  def postTolerant = Action(circe.tolerantJson[Foo]) { implicit request =>
    val isEqual = request.body == Data.foo
    Ok(isEqual.toString)
  }

  def postTolerantJson = Action(circe.tolerantJson) { implicit request =>
    val isEqual = request.body == Data.foo.asJson
    Ok(isEqual.toString)
  }
}

object CirceController {
  implicit val actorSystem: ActorSystem     = ActorSystem()
  implicit val ec: ExecutionContextExecutor = actorSystem.dispatcher
}
