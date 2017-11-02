package play.api.libs.circe

import io.circe.Printer
import io.circe.generic.auto._
import io.circe.syntax._
import play.api.mvc._

object CirceController extends Controller with Circe {

  implicit val customPrinter = Printer.spaces2.copy(dropNullValues = true)

  def get = Action {
    Ok(Conf.foo.asJson)
  }

  def post = Action(circe.json[Foo]) { implicit request =>
    val isEqual = request.body == Conf.foo
    Ok(isEqual.toString)
  }

  def postJson = Action(circe.json) { implicit request =>
    val isEqual = request.body == Conf.foo.asJson
    Ok(isEqual.toString)
  }

  def postTolerate = Action(circe.tolerantJson[Foo]) { implicit request =>
    val isEqual = request.body == Conf.foo
    Ok(isEqual.toString)
  }

  def postTolerateJson = Action(circe.tolerantJson) { implicit request =>
    val isEqual = request.body == Conf.foo.asJson
    Ok(isEqual.toString)
  }
}
