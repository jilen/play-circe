package play.api.libs.circe

import io.circe.generic.auto._
import io.circe.syntax._
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api._
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.inject.guice._
import scala.concurrent._

class CirceSpec extends PlaySpec with GuiceOneServerPerSuite {

  private lazy val controllersComponent = app.injector.instanceOf[ControllerComponents]
  private lazy val circeController      = new CirceController(controllersComponent)
  private def wsClient             = app.injector.instanceOf[WSClient]
  private lazy val url                  = s"http://127.0.0.1:$port"
  private lazy val fooJsonString        = circeController.customPrinter.print(Data.foo.asJson)

  private def await[A](f: Future[A]): A = Await.result(f, duration.Duration.Inf)

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .appRoutes( app => {
        case ("GET", "/get")               => circeController.get
        case ("POST", "/post")             => circeController.post
        case ("POST", "/postJson")         => circeController.postJson
        case ("POST", "/postTolerant")     => circeController.postTolerant
        case ("POST", "/postTolerantJson") => circeController.postTolerantJson
      })
      .build()

  "Circe trait" must {
    "server json" in {
      val resp = await(wsClient.url(url + "/get").get())
      resp.headers("Content-Type").head mustEqual "application/json"
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
