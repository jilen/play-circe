package play.api.libs.circe

import akka.stream.scaladsl.{Flow, Sink}
import akka.util.ByteString
import cats.syntax.all._
import io.circe.{Codec => _, _}
import org.slf4j._
import play.api.http._
import play.api.libs.streams.Accumulator
import play.api.libs.streams.Execution.Implicits.trampoline
import play.api.mvc._

import scala.concurrent.Future
import scala.util.Try
import scala.util.control.NonFatal

trait Circe extends Status {

  def parse: PlayBodyParsers

  private val defaultPrinter = Printer.noSpaces

  protected def circeErrorHandler: HttpErrorHandler = new DefaultHttpErrorHandler

  protected def onCirceError(e: Error): Result = Results.BadRequest(e.show)

  private final val logger = LoggerFactory.getLogger(classOf[Circe])

  implicit val contentTypeOf_Json: ContentTypeOf[Json] = ContentTypeOf(Some(ContentTypes.JSON))

  implicit def writableOf_Json(implicit
      codec: Codec,
      printer: Printer = defaultPrinter
  ): Writeable[Json] = Writeable(a => codec.encode(printer.print(a)))

  object circe {

    def json[T: Decoder]: BodyParser[T] = json.validate(decodeJson[T])

    def json: BodyParser[Json] = json(parse.DefaultMaxTextLength)

    def json(maxLength: Long): BodyParser[Json] = parse.when(
      _.contentType.exists(m =>
        m.equalsIgnoreCase("text/json") || m
          .equalsIgnoreCase("application/json")
      ),
      tolerantJson(maxLength),
      createBadResult(
        "Expecting text/json or application/json body",
        UNSUPPORTED_MEDIA_TYPE
      )
    )

    def tolerantJson[T: Decoder]: BodyParser[T] = tolerantJson.validate(decodeJson[T])

    def tolerantJson: BodyParser[Json] = tolerantJson(parse.DefaultMaxTextLength)

    def tolerantJson(maxLength: Long): BodyParser[Json] = {
      tolerantBodyParser[Json]("json", maxLength, "Invalid Json") { (request, bytes) =>
        val bodyString = new String(bytes.toArray[Byte], detectCharset(request))
        parser.parse(bodyString).leftMap(onCirceError)
      }
    }

    private def detectCharset(request: RequestHeader) = {
      val CharsetPattern = "(?i)\\bcharset=\\s*\"?([^\\s;\"]*)".r
      request.headers.get("Content-Type") match {
        case Some(CharsetPattern(c)) => c
        case _                       => "UTF-8"
      }
    }

    private def decodeJson[T: Decoder](json: Json) = {
      json.as[T].leftMap { ex =>
        if (logger.isDebugEnabled) {
          logger.debug(s"Cannot decode json $json", ex)
        }
        onCirceError(ex)
      }
    }

    private def createBadResult(
        msg: String,
        statusCode: Int = BAD_REQUEST
    ): RequestHeader => Future[Result] = { request =>
      circeErrorHandler.onClientError(request, statusCode, msg)
    }

    private def tolerantBodyParser[A](
        name: String,
        maxLength: Long,
        errorMessage: String
    )(
        parser: (RequestHeader, ByteString) => Either[Result, A]
    ): BodyParser[A] = {
      BodyParser(name + ", maxLength=" + maxLength) { request =>
        def parseBody(bytes: ByteString): Future[Either[Result, A]] = {
          Future.fromTry(Try(parser(request, bytes))).recoverWith { case NonFatal(e) =>
            logger.debug(errorMessage, e)
            createBadResult(errorMessage + ": " + e.getMessage)(request)
              .map(Left(_))
          }
        }

        Accumulator.strict[ByteString, Either[Result, A]](
          // If the body was strict
          {
            case Some(bytes) if bytes.size <= maxLength => parseBody(bytes)
            case None                                   => parseBody(ByteString.empty)
            case _                                      =>
              createBadResult("Request Entity Too Large", REQUEST_ENTITY_TOO_LARGE)(request).map(Left.apply)
          },
          // Otherwise, use an enforce max length accumulator on a folding sink
          enforceMaxLength(
            request,
            maxLength,
            Accumulator(
              Sink.fold[ByteString, ByteString](ByteString.empty)((state, bs) => state ++ bs)
            ).mapFuture(parseBody)
          ).toSink
        )
      }
    }

    private[play] def enforceMaxLength[A](
        request: RequestHeader,
        maxLength: Long,
        accumulator: Accumulator[ByteString, Either[Result, A]]
    ): Accumulator[ByteString, Either[Result, A]] = {
      val takeUpToFlow = Flow.fromGraph(new BodyParsers.TakeUpTo(maxLength.toLong))
      Accumulator(takeUpToFlow.toMat(accumulator.toSink) { (statusFuture, resultFuture) =>
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
