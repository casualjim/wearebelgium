package be.wearebelgium.ext

import net.liftweb.json._

class JValueExt(json: JValue) {

  import mojolly.inflector.InflectorImports.string2InflectorString

  def camelizeKeys = rewriteJsonAST(camelize = true)
  def snakizeKeys = rewriteJsonAST(camelize = false)

  private def rewriteJsonAST(camelize: Boolean): JValue = {
    json transform {
      case JField(nm, x) if !nm.startsWith("_") ⇒ JField(if (camelize) nm.camelize else nm.underscore, x)
      case x                                    ⇒ x
    }
  }
}