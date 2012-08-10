package be.wearebelgium

import mojolly.inflector.InflectorImports
import io.Codec
import org.scala_tools.time.{ StaticDateTime, Imports }
import org.joda.time.format.ISODateTimeFormat
import com.mongodb.casbah.query.dsl.FluidQueryBarewordOps
import com.mongodb.casbah.{ query, commons }
import query.{ ValidDateOrNumericTypeHolder, ValidNumericTypeHolder, ValidDateTypeHolder, ValidBarewordExpressionArgTypeHolder }
import java.util.Locale
import java.net.URI
import org.scalatra.ScalatraBase
import net.liftweb.json._
import util.DateFormats

package object tweets
    extends Imports
    with InflectorImports
    with com.mongodb.casbah.Imports
    with FluidQueryBarewordOps
    with commons.Imports
    with query.Imports
    with ValidBarewordExpressionArgTypeHolder
    with ValidDateTypeHolder
    with ValidNumericTypeHolder
    with ValidDateOrNumericTypeHolder
    with _root_.org.scalatra.servlet.ServletApiImplicits {

  val BrusselsZone = DateTimeZone.forID("Europe/Brussels")
  DateTimeZone.setDefault(BrusselsZone)

  implicit def staticDateTimeExt(d: StaticDateTime) = new {
    def today = d.now.toDateMidnight.toDateTime
    def thisWeek = {
      val n = d.now
      DefaultWeek(n.weekOfWeekyear.get, n.year.get)
    }
  }

  implicit def dateTime2JValue(d: DateTime): JValue = JString(d.toString(DateFormats.Iso8601NoMillis.dateTimeformat))
  implicit def string2RicherString(s: String) = new ext.StringExt(s)
  implicit def jvalue2RicherJValue(j: JValue) = new ext.JValueExt(j)
  implicit def byteArray2Richer(arr: Array[Byte]) = new ext.ByteArrayExt(arr)
  implicit def uri2richerUri(uri: URI) = new ext.UriExt(uri)
  implicit def servletBase2RicherServletBase(base: ScalatraBase) = new ext.ScalatraBaseExt(base)
  implicit def extendedDateTime(d: DateTime) = new ext.DateTimeExt(d)

  implicit def dateTimeWithWeeks(d: DateTime) = new {
    def asWeek = DefaultWeek(d.weekOfWeekyear.get, d.year.get)
  }

  val ENGLISH = Locale.ENGLISH
  val UTF_8 = Codec.UTF8.name()
  val Utf8 = Codec.UTF8
  val UTC_STR = "UTC"
  val UTC = DateTimeZone.UTC

  val MinDate = new DateTime(0L)
  val minDateCal = MinDate.toCalendar(ENGLISH)
  val minDate = MinDate.toDate
  val Iso8601DateNoMillis = ISODateTimeFormat.dateTimeNoMillis.withZone(UTC)
  val Iso8601Date = ISODateTimeFormat.dateTime.withZone(UTC)
}