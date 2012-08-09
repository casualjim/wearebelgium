package be.wearebelgium.ext

import org.scalatra.ScalatraBase
import org.scalatra.util.RicherString._
import org.scalatra.servlet.ServletApiImplicits._
import java.util.Locale

class ScalatraBaseExt(base: ScalatraBase) {

  def remoteAddress = base.request.remoteAddress

  def isHttps = { // also respect load balancer version of the protocol
    def h = base.request.headers.get("X-FORWARDED-PROTO").flatMap(_.blankOption).map(_.toUpperCase(Locale.ENGLISH))
    base.request.isSecure || h == Some("HTTPS")
  }

}