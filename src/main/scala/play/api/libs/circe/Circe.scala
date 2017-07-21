package play.api.libs.circe

import cats.syntax.either._
import cats.syntax.show._
import akka.stream.scaladsl.{ Flow, Sink}
import akka.util.ByteString
import io.circe._
import play.api.http._
import play.api.libs.streams.Execution.Implicits.trampoline
import play.api.libs.streams.Accumulator
import play.api.Logger
import play.api.mvc._
import scala.concurrent.Future
import scala.util.control.NonFatal

trait Circe extends Status {

  private val defaultPrinter = Printer.noSpaces

  def parse: PlayBodyParsers

  implicit def contentTypeOf_Json(implicit codec: Codec): ContentTypeOf[Json] = {
    ContentTypeOf(Some(ContentTypes.JSON))
  }

  implicit def writableOf_Json(implicit codec: Codec, printer: Printer = defaultPrinter): Writeable[Json] = {
    Writeable(a => codec.encode(a.pretty(printer)))
  }

  object circe {

    val logger = Logger(classOf[Circe])

    def json[T: Decoder]: BodyParser[T] = json.validate(decodeJson[T])

    def json: BodyParser[Json] = json(parse.DefaultMaxTextLength)

    def json(maxLength: Int): BodyParser[Json] = parse.when(
      _.contentType.exists(m => m.equalsIgnoreCase("text/json") || m.equalsIgnoreCase("application/json")),
      tolerantJson(maxLength),
      createBadResult("Expecting text/json or application/json body", UNSUPPORTED_MEDIA_TYPE)
    )

    def tolerantJson[T: Decoder]: BodyParser[T] = tolerantJson.validate(decodeJson[T])

    def tolerantJson: BodyParser[Json] = tolerantJson(parse.DefaultMaxTextLength)

    def tolerantJson(maxLength: Int): BodyParser[Json] = {
      tolerantBodyParser[Json]("json", maxLength, "Invalid Json") { (request, bytes) =>
        val bodyString = new String(bytes.toArray[Byte], detectCharset(request))
        parser.parse(bodyString).leftMap(onCirceError)
      }
    }

    protected def onCirceError(e: Error): Result = {
      Results.BadRequest(e.show)
    }

    private def detectCharset(request: RequestHeader) = {
      val CharsetPattern = "(?i)\\bcharset=\\s*\"?([^\\s;\"]*)".r
      request.headers.get("Content-Type") match {
        case Some(CharsetPattern(c)) => c
        case _ => "UTF-8"
      }
    }

    private def decodeJson[T: Decoder](json: Json) = {
      implicitly[Decoder[T]].decodeJson(json).leftMap { ex =>
        logger.debug(s"Cannot decode json $json", ex)
        Results.BadRequest("Cannot decode request")
      }
    }

    private def createBadResult(msg: String, statusCode: Int = BAD_REQUEST): RequestHeader => Future[Result] = { request =>
      LazyHttpErrorHandler.onClientError(request, statusCode, msg)
    }

    private def tolerantBodyParser[A](name: String, maxLength: Int, errorMessage: String)(parser: (RequestHeader, ByteString) => Either[Result, A]): BodyParser[A] = {
      BodyParser(name + ", maxLength=" + maxLength) { request =>
        import play.core.Execution.Implicits.trampoline

        def parseBody(bytes: ByteString): Future[Either[Result, A]] = {
          try {
            Future.successful(parser(request, bytes))
          } catch {
            case NonFatal(e) =>
              logger.debug(errorMessage, e)
              createBadResult(errorMessage + ": " + e.getMessage)(request).map(Left(_))
          }
        }

        Accumulator.strict[ByteString, Either[Result, A]](
          // If the body was strict
          {
            case Some(bytes) if bytes.size <= maxLength =>
              parseBody(bytes)
            case None =>
              parseBody(ByteString.empty)
            case _ =>
              createBadResult("Request Entity Too Large", REQUEST_ENTITY_TOO_LARGE)(request).map(Left.apply)
          },
          // Otherwise, use an enforce max length accumulator on a folding sink
          enforceMaxLength(request, maxLength, Accumulator(
            Sink.fold[ByteString, ByteString](ByteString.empty)((state, bs) => state ++ bs)
          ).mapFuture(parseBody)).toSink
        )
      }
    }


    private[play] def enforceMaxLength[A](request: RequestHeader, maxLength: Int, accumulator: Accumulator[ByteString, Either[Result, A]]): Accumulator[ByteString, Either[Result, A]] = {
      val takeUpToFlow = Flow.fromGraph(new BodyParsers.TakeUpTo(maxLength.toLong))
      Accumulator(takeUpToFlow.toMat(accumulator.toSink) { (statusFuture, resultFuture) =>
        import play.core.Execution.Implicits.trampoline
        statusFuture.flatMap {
          case MaxSizeExceeded(_) =>
            val badResult = createBadResult("Request Entity Too Large", REQUEST_ENTITY_TOO_LARGE)(request)
            badResult.map(Left(_))

          case MaxSizeNotExceeded =>
            resultFuture
        }
      })
    }
  }

}
