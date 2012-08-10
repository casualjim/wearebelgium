package be.wearebelgium.tweets

import scala.util.control.Exception.ignoring
import com.mongodb.casbah.commons.conversions.scala.{ DeregisterJodaTimeConversionHelpers, RegisterJodaTimeConversionHelpers }
import java.net.URI

object MongoConfiguration {
  def apply(uri: String): MongoConfiguration = MongoConfiguration(new URI(uri))
}
case class MongoConfiguration(uri: URI) extends be.wearebelgium.util.Logging {

  logger.info("Connecting to mongodb with: %s" format uri.toASCIIString)

  private val userInfo: Option[(String, String)] = {
    uri.getUserInfo.blankOption map { uif ⇒
      val Array(user, secret) = if (uif.indexOf(":") > -1) (uif.toString split ':') else Array(uif, "")
      user -> secret
    }
  }

  def isAuthenticated = uri.getUserInfo.blankOption.isDefined

  var _db: MongoDB = null
  var _conn: MongoConnection = null

  def connection = synchronized {
    if (_conn == null) {
      _conn = MongoConnection(uri.getHost, uri.getPort)
      RegisterJodaTimeConversionHelpers()
    }
    _conn
  }

  def disconnect = synchronized {
    ignoring(classOf[Throwable]) { Option(_conn) foreach { _.close() } }
    DeregisterJodaTimeConversionHelpers()
    _conn = null
    _db = null
  }

  def db = synchronized {
    if (_db == null) {
      val db = connection(uri.getPath.substring(1))
      userInfo foreach {
        case (user, pass) ⇒ db.authenticate(user, pass)
      }
      _db = db
    }
    _db
  }

}
