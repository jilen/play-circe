package play.api.libs.circe

import io.circe.Printer
import io.circe.generic.auto._
import io.circe.syntax._
import play.api.mvc._

object CustomPrinterCirceController extends Controller with Circe {

  implicit val customPrinter = Printer.spaces2.copy(dropNullKeys = true)

  def get = Action {
    Ok(Conf.foo.asJson)
  }

}
