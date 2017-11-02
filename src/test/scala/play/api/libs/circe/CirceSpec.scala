package play.api.libs.circe

import io.circe.generic.auto._
import io.circe.syntax._
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api._
import play.api.libs.ws.WSClient
import play.api.routing._
import play.api.routing.sird._
import play.api.mvc._
import play.api.inject.guice._
import scala.concurrent._

class CirceSpec extends PlaySpec with GuiceOneServerPerSuite {

  lazy val controllersComponent = app.injector.instanceOf[ControllerComponents]
  lazy val circeController = new CirceController(controllersComponent)
  lazy val wsClient = app.injector.instanceOf[WSClient]
  lazy val url = s"http://127.0.0.1:$port"
  lazy val fooJsonString = circeController.customPrinter.pretty(Data.foo.asJson)

  def await[A](f: Future[A]) = Await.result(f, duration.Duration.Inf)


  override def fakeApplication(): Application =
    new GuiceApplicationBuilder().router(Router.from {
      case GET(p"/get") => circeController.get
      case POST(p"/post") => circeController.post
      case POST(p"/postJson") => circeController.postJson
      case POST(p"/postTolerant") => circeController.postTolerant
      case POST(p"/postTolerantJson") => circeController.postTolerantJson
    }).build()


  "Circe trait"  must {
    "server json" in {
      val resp = await(wsClient.url(url + "/get").get())
      resp.headers("Content-Type")(0) mustEqual("application/json")
      resp.body mustEqual fooJsonString
    }
    "parse json as object" in {
      val resp = wsClient
        .url(url + "/post")
        .addHttpHeaders("Content-Type" -> "application/json")
        .post(fooJsonString)
      await(resp).body mustEqual "true"
    }
    "parse json" in {
      val resp = wsClient
        .url(url + "/postJson")
        .addHttpHeaders("Content-Type" -> "application/json")
        .post(fooJsonString)
      await(resp).body mustEqual "true"
    }
    "parse json as obj for content type `text/html`" in {
      val resp = wsClient
        .url(url + "/postTolerant")
        .post(fooJsonString)
      await(resp).body mustEqual "true"
    }
    "parse json for content type `text/html`" in {
      val resp = wsClient
        .url(url + "/postTolerantJson")
        .post(fooJsonString)
      await(resp).body mustEqual "true"
    }
    "report 415 while parsing non-json content-type" in {
      val resp = wsClient
        .url(url + "/post")
        .post(fooJsonString)
      await(resp).status mustEqual 415
    }
    "report 400 while parsing invalid json String" in {
      val resp = wsClient
        .url(url + "/post")
        .withHttpHeaders("Content-Type" -> "application/json")
        .post("invalid json string")
      await(resp).status mustEqual 400
    }
    "report 400 if decode failed" in {
      val resp = wsClient
        .url(url + "/post")
        .withHttpHeaders("Content-Type" -> "application/json")
        .post("{}")
      await(resp).status mustEqual 400
    }
  }
}
