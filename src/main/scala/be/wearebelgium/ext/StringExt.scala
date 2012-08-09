package be.wearebelgium.ext

import scala.Some
import rl.UrlCodingUtils
import java.nio.charset.Charset
import mojolly.inflector.Inflector
import org.apache.commons.codec.binary.Hex
import be.wearebelgium.util.DateFormats.DateFormat
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import be.wearebelgium.util.DateFormats
import java.util.Locale.ENGLISH

class StringExt(s: String) {
  def blankOption = if (isBlank) None else Some(s)
  def isBlank = s == null || s.trim.isEmpty
  def nonBlank = !isBlank
  def asCheckboxBool = s.toUpperCase(ENGLISH) match {
    case "ON" | "TRUE" | "OK" | "1" | "CHECKED" ⇒ true
    case _                                      ⇒ false
  }
  def urlEncode: String = { // Encoding comforming to RFC 3986
    UrlCodingUtils.urlEncode(s)
  }
  def urlEncode(charset: Charset): String = { // Encoding comforming to RFC 3986
    UrlCodingUtils.urlEncode(s, charset)
  }
  def formEncode: String = { // This gives the same output as java.net.URLEncoder
    UrlCodingUtils.urlEncode(s, spaceIsPlus = true)
  }
  def formEncode(charset: Charset): String = { // This gives the same output as java.net.URLEncoder
    UrlCodingUtils.urlEncode(s, charset, spaceIsPlus = true)
  }
  def urlDecode: String = {
    UrlCodingUtils.urlDecode(s, plusIsSpace = false)
  }

  def formDecode: String = { // This gives the same output as java.net.URLDecoder
    UrlCodingUtils.urlDecode(s, plusIsSpace = true)
  }
  def urlDecode(charset: Charset): String = {
    UrlCodingUtils.urlDecode(s, charset, plusIsSpace = false)
  }

  def formDecode(charset: Charset): String = { // This gives the same output as java.net.URLDecoder
    UrlCodingUtils.urlDecode(s, charset, plusIsSpace = true)
  }

  def %%(params: Map[String, String]) = Inflector.interpolate(s, params)

  def hexDecode() = Hex.decodeHex(s.toCharArray)

  def asDate(format: String): Option[DateTime] = new DateFormat {
    val dateTimeformat = DateTimeFormat.forPattern(format)
  }.parse(s)

  def asWebDate: Option[DateTime] = DateFormats.parse(s)

  def asIso8601Date: Option[DateTime] = DateFormats(DateFormats.Iso8601, DateFormats.Iso8601NoMillis).parse(s)
}

