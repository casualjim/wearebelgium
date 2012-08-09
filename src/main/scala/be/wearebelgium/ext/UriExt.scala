package be.wearebelgium.ext

import rl.MapQueryString
import java.net.URI
import org.scalatra.util.RicherString._

class UriExt(uri: URI) {

  private var _query: Map[String, Seq[String]] = Map.empty

  def query = {
    if (_query == Map.empty[String, Seq[String]] && uri.getQuery.nonBlank)
      _query ++= MapQueryString.parseString(uri.getQuery)

    _query
  }

  def resources = parsePath(uri.getPath)

  private def parsePath(path: String): Seq[String] = {
    val ps = if (path.nonBlank && path.startsWith("/")) path.substring(1) else path
    if (ps.nonBlank) ps.split("/").map(_.urlDecode).toList else Nil
  }
}
