package play.api.libs.circe

import akka.util.ByteString

import java.net.URI
import java.security.cert.X509Certificate

import play.api.mvc.{Headers, RequestHeader}
import play.api.mvc.request._
import play.api.http.HttpConfiguration
import play.core.parsers.FormUrlEncodedParser
import play.api.libs.typedmap.TypedMap

trait Fakes {

  case class FakeReq(
      method: String,
      path: String,
      headers: Seq[(String, String)],
      body: ByteString
  ) {

    def withBody(contentType: String, body: ByteString): FakeReq = {
      copy(body = body, headers = headers :+ ("Content-Type" -> contentType))
    }

    def withTextBody(contentType: String, body: String): FakeReq = {
      copy(body = ByteString(body.getBytes()), headers = headers :+ ("Content-Type" -> contentType))
    }

    def requestHeader: RequestHeader = fakeHeaders(method = method, uri = path, hds = headers)
  }

  object FakeReq {
    def get(path: String): FakeReq = FakeReq(method = "GET", path = path, headers = Seq.empty, body = ByteString.empty)
    def post(path: String): FakeReq =
      FakeReq(method = "POST", path = path, headers = Seq.empty, body = ByteString.empty)
  }

  private val requestFactory = new DefaultRequestFactory(HttpConfiguration())

  def fakeHeaders(
      method: String,
      uri: String,
      hds: Seq[(String, String)]
  ): RequestHeader = {
    val remoteAddress                                        = "127.0.0.1"
    val version                                              = "HTTP/1.1"
    val id                                                   = 666
    val secure: Boolean                                      = false
    val clientCertificateChain: Option[Seq[X509Certificate]] = None
    val attrs: TypedMap                                      = TypedMap.empty
    val conn = RemoteConnection(remoteAddress, secure, clientCertificateChain)
    val _uri = uri
    val target = new RequestTarget {
      override lazy val uri: URI                           = new URI(uriString)
      override def uriString: String                       = _uri
      override lazy val path                               = uriString.split('?').take(1).mkString
      override lazy val queryMap: Map[String, Seq[String]] = FormUrlEncodedParser.parse(queryString)
    }
    requestFactory.createRequestHeader(
      connection = conn,
      method = method,
      target = target,
      version = version,
      headers = Headers(hds: _*),
      attrs = attrs + (RequestAttrKey.Id -> id)
    )
  }
}
