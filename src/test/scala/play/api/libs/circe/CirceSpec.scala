package play.api.libs.circe

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import akka.util.ByteString
import io.circe.syntax._
import play.api.mvc._
import play.api.inject.guice._
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global

class CirceSuite extends munit.FunSuite with Fakes {
  val app                               = GuiceApplicationBuilder().build()
  private lazy val controllersComponent = app.injector.instanceOf[ControllerComponents]
  private lazy val circeController      = new CirceController(controllersComponent)
  private lazy val fooJsonString        = circeController.customPrinter.print(Data.foo.asJson)

  implicit val actorSystem: ActorSystem = app.actorSystem

  case class Reply(
      status: Int,
      contentType: Option[String],
      body: String
  )

  def call(action: EssentialAction, hd: RequestHeader, body: ByteString)(implicit
      mat: Materializer
  ): Future[Reply] = {
    action(hd).run(Source.single(body)).flatMap { (result) =>
      val body = result.body
      val cs = body.contentType
        .flatMap { s =>
          if (s.contains("charset=")) Some(s.split("; *charset=").drop(1).mkString.trim) else None
        }
        .getOrElse("utf-8")
      body.consumeData.map(_.decodeString(cs)).map { bd =>
        Reply(result.header.status, body.contentType, bd)
      }
    }
  }

  def call(action: EssentialAction, req: FakeReq): Future[Reply] = {
    val hds = req.requestHeader
    call(action, hds, req.body)
  }

  test("server json") {
    call(circeController.get, FakeReq.get("/get")).map { case Reply(_, ct, bd) =>
      assertEquals(ct, Some("application/json"))
      assertEquals(bd, fooJsonString)
    }
  }

  test("parse case class") {
    call(circeController.post, FakeReq.post("/post").withTextBody("application/json", fooJsonString)).map {
      case Reply(_, ct, bd) =>
        assertEquals(ct, Some("text/plain; charset=utf-8"))
        assertEquals(bd, "true")
    }
  }
  test("parse json") {
    call(circeController.postJson, FakeReq.post("/postJson").withTextBody("application/json", fooJsonString)).map {
      case Reply(_, ct, bd) =>
        assertEquals(ct, Some("text/plain; charset=utf-8"))
        assertEquals(bd, "true")
    }
  }

  test("parse `plain/text` as json") {
    call(
      circeController.postTolerantJson,
      FakeReq.post("/postTolerantJson").withTextBody("text/plain;charset=utf-8", fooJsonString)
    ).map { case Reply(_, ct, bd) =>
      assertEquals(ct, Some("text/plain; charset=utf-8"))
      assertEquals(bd, "true")
    }
  }

  test("parse `text/html` as json") {
    call(
      circeController.postTolerantJson,
      FakeReq.post("/postTolerantJson").withTextBody("text/html;charset=utf-8", fooJsonString)
    ).map { case Reply(_, ct, bd) =>
      assertEquals(ct, Some("text/plain; charset=utf-8"))
      assertEquals(bd, "true")
    }
  }

  test("invalid content-type") {
    call(circeController.post, FakeReq.post("/post").withTextBody("text/html;charset=utf-8", fooJsonString)).map {
      case Reply(s, ct, bd) =>
        assertEquals(s, 415)
    }
  }
  test("invalid json string") {
    call(circeController.post, FakeReq.post("/post").withTextBody("application/json", "not a json string")).map {
      case Reply(s, ct, bd) =>
        assertEquals(s, 400)
    }
  }
  test("report 400 if decode failed") {
    call(circeController.post, FakeReq.post("/post").withTextBody("application/json", "{}")).map {
      case Reply(s, ct, bd) =>
        assertEquals(s, 400)
    }
  }

}
