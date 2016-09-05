package play.api.libs.circe

import akka.stream.scaladsl.{ Flow, Sink}
import akka.util.ByteString
import io.circe._
import cats.data.Xor
import play.api.http._
import play.api.http.Status._
import play.api.libs.iteratee.Execution.Implicits.trampoline
import play.api.libs.streams.Accumulator
import play.api.Logger
import play.api.mvc._
import scala.concurrent.Future
import scala.util.control.NonFatal

trait Circe  {

  implicit def contentTypeOf_Json(implicit codec: Codec): ContentTypeOf[Json] = {
    ContentTypeOf(Some(ContentTypes.JSON))
  }

  implicit def writableOf_Json(implicit codec: Codec): Writeable[Json] = {
    Writeable(a => codec.encode(a.noSpaces))
  }

  object circe {

    import BodyParsers._

    @inline def DefaultMaxTextLength: Long = parse.DefaultMaxTextLength.toLong

    val logger = Logger(BodyParsers.getClass)

    def json[T: Decoder]: BodyParser[T] = json.mapM { json =>
      implicitly[Decoder[T]].decodeJson(json) match {
        case Xor.Left(e) => Future.failed(e)
        case Xor.Right(t) => Future.successful(t)
      }
    }

    def json: BodyParser[Json] = json(DefaultMaxTextLength)

    def json(maxLength: Long): BodyParser[Json] = parse.when(
      _.contentType.exists(m => m.equalsIgnoreCase("text/json") || m.equalsIgnoreCase("application/json")),
      tolerantJson(maxLength),
      createBadResult("Expecting text/json or application/json body", UNSUPPORTED_MEDIA_TYPE)
    )

    def tolerantJson[T: Decoder]: BodyParser[T] = tolerantJson.mapM { json =>
      implicitly[Decoder[T]].decodeJson(json) match {
        case Xor.Left(e) => Future.failed(e)
        case Xor.Right(t) => Future.successful(t)
      }
    }

    def tolerantJson: BodyParser[Json] = tolerantJson(DefaultMaxTextLength)

    def tolerantJson(maxLength: Long): BodyParser[Json] = {
      tolerantBodyParser[Json]("json", maxLength, "Invalid Json") { (request, bytes) =>
        jawn.parseByteBuffer(bytes.toByteBuffer).valueOr(throw _)
      }
    }

    private def createBadResult(msg: String, statusCode: Int = BAD_REQUEST): RequestHeader => Future[Result] = { request =>
      LazyHttpErrorHandler.onClientError(request, statusCode, msg)
    }

    /**
     * Create a body parser that uses the given parser and enforces the given max length.
     *
     * @param name The name of the body parser.
     * @param maxLength The maximum length of the body to buffer.
     * @param errorMessage The error message to prepend to the exception message if an error was encountered.
     * @param parser The parser.
     */
    private def tolerantBodyParser[A](name: String, maxLength: Long, errorMessage: String)(parser: (RequestHeader, ByteString) => A): BodyParser[A] =
      BodyParser(name + ", maxLength=" + maxLength) { request =>
        import play.api.libs.iteratee.Execution.Implicits.trampoline

        enforceMaxLength(request, maxLength, Accumulator(
          Sink.fold[ByteString, ByteString](ByteString.empty)((state, bs) => state ++ bs)
        ) mapFuture { bytes =>
          try {
            Future.successful(Right(parser(request, bytes)))
          } catch {
            case NonFatal(e) =>
              logger.debug(errorMessage, e)
              createBadResult(errorMessage + ": " + e.getMessage)(request).map(Left(_))
          }
        })
      }



    private def enforceMaxLength[A](request: RequestHeader, maxLength: Long, accumulator: Accumulator[ByteString, Either[Result, A]]): Accumulator[ByteString, Either[Result, A]] = {
      val takeUpToFlow = Flow.fromGraph(new BodyParsers.TakeUpTo(maxLength))
      Accumulator(takeUpToFlow.toMat(accumulator.toSink) { (statusFuture, resultFuture) =>
        import play.api.libs.iteratee.Execution.Implicits.trampoline
        val defaultCtx = play.api.libs.concurrent.Execution.Implicits.defaultContext

        statusFuture.flatMap {
          case MaxSizeExceeded(_) =>
            val badResult = Future.successful(()).flatMap(_ => createBadResult("Request Entity Too Large", REQUEST_ENTITY_TOO_LARGE)(request))(defaultCtx)
            badResult.map(Left(_))
          case MaxSizeNotExceeded => resultFuture
        }
      })
    }
  }
}
