package play.api.libs.circe

import cats.syntax.either._
import io.circe._
import play.api.http._
import play.api.http.Status._
import play.api.libs.iteratee._
import play.api.libs.iteratee.Execution.Implicits.trampoline
import play.api.Logger
import play.api.mvc._
import scala.concurrent.Future

trait Circe  {

  private val defaultPrinter = Printer.noSpaces

  implicit def contentTypeOf_Json(implicit codec: Codec): ContentTypeOf[Json] = {
    ContentTypeOf(Some(ContentTypes.JSON))
  }

  implicit def writableOf_Json(implicit codec: Codec, printer: Printer = defaultPrinter): Writeable[Json] = {
    Writeable(a => codec.encode(a.pretty(printer)))
  }

  object circe {

    import BodyParsers._

    @inline def DefaultMaxTextLength: Long = parse.DefaultMaxTextLength.toLong

    val logger = Logger(classOf[Circe])

    def json[T: Decoder]: BodyParser[T] = json.validate(decodeJson[T])

    def json: BodyParser[Json] = json(DefaultMaxTextLength)

    def json(maxLength: Long): BodyParser[Json] = parse.when(
      _.contentType.exists(m => m.equalsIgnoreCase("text/json") || m.equalsIgnoreCase("application/json")),
      tolerantJson(maxLength),
      createBadResult("Expecting text/json or application/json body", UNSUPPORTED_MEDIA_TYPE)
    )

    def tolerantJson[T: Decoder]: BodyParser[T] = tolerantJson.validate(decodeJson[T])

    def tolerantJson: BodyParser[Json] = tolerantJson(DefaultMaxTextLength)

    def tolerantJson(maxLength: Long): BodyParser[Json] = {
      tolerantBodyParser[Json]("json", maxLength, "Invalid Json") { (request, bytes) =>
        val bodyString = new String(bytes, "UTF-8")
        val result = parser.parse(bodyString)
        result match {
          case Left(err) =>
            logger.info(s"Invalid json string $bodyString", err)
          case Right(obj) =>
        }
        result
      }
    }

    private def decodeJson[T: Decoder](json: Json) = {
      implicitly[Decoder[T]].decodeJson(json).leftMap { err =>
        logger.debug(s"Cannot decode ${json}", err)
        Results.BadRequest("Cannot decode request")
      }
    }

    private def createBadResult(msg: String, statusCode: Int = BAD_REQUEST): RequestHeader => Future[Result] = { request =>
      LazyHttpErrorHandler.onClientError(request, statusCode, msg)
    }

    private def tolerantBodyParser[A](name: String, maxLength: Long, errorMessage: String)(parser: (RequestHeader, Array[Byte]) => Either[Error, A]): BodyParser[A] =
      BodyParser(name + ", maxLength=" + maxLength) { request =>
        import play.api.libs.iteratee.Execution.Implicits.trampoline
        import scala.util.control._

        val bodyParser: Iteratee[Array[Byte], Either[Result, Either[Future[Result], A]]] =
          Traversable.takeUpTo[Array[Byte]](maxLength).transform(
            Iteratee.consume[Array[Byte]]().map { bytes =>
              parser(request, bytes).left.map {
                case NonFatal(e) =>
                  logger.debug(errorMessage, e)
                  createBadResult(errorMessage + ": " + e.getMessage)(request)
                case t => throw t
              }
            }
          ).flatMap(checkForEof(request))

        bodyParser.mapM {
          case Left(tooLarge) => Future.successful(Left(tooLarge))
          case Right(Left(badResult)) => badResult.map(Left.apply)
          case Right(Right(body)) => Future.successful(Right(body))
        }
      }

    /**
     * Check that the input is finished. If it is finished, the iteratee returns `eofValue`.
     * If the input is not finished then it returns a REQUEST_ENTITY_TOO_LARGE result.
     */
    private def checkForEof[A](request: RequestHeader): A => Iteratee[Array[Byte], Either[Result, A]] = { eofValue: A =>
      import play.api.libs.iteratee.Execution.Implicits.trampoline
      def cont: Iteratee[Array[Byte], Either[Result, A]] = Cont {
        case in @ Input.El(e) =>
          val badResult: Future[Result] = createBadResult("Request Entity Too Large", REQUEST_ENTITY_TOO_LARGE)(request)
          Iteratee.flatten(badResult.map(r => Done(Left(r), in)))
        case in @ Input.EOF =>
          Done(Right(eofValue), in)
        case Input.Empty =>
          cont
      }
      cont
    }
  }
}
