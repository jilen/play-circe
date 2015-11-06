package play.api.libs.circe

import io.circe.generic.auto._
import io.circe.parse._
import io.circe.syntax._
import play.api._
import play.api.mvc._

object CirceController extends Controller with Circe {

  def get = Action {
    Ok(Conf.foo.asJson)
  }

  def post = Action(circe.json[Foo]) { implicit request =>
    val isEqual = request.body == Conf.foo
    Ok(isEqual.toString)
  }
}
