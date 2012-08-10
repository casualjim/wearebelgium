package be.wearebelgium

import org.scala_tools.time.StaticDateTime

package object tweets extends util.GlobalImports {

  val BrusselsZone = DateTimeZone.forID("Europe/Brussels")
  DateTimeZone.setDefault(BrusselsZone)

  implicit def dateTimeWithWeeks(d: DateTime) = new {
    def asWeek = DefaultWeek(d.weekOfWeekyear.get, d.year.get)
  }

  implicit def staticDateTimeExt(d: StaticDateTime) = new {
    def today = d.now.toDateMidnight.toDateTime
    def thisWeek = {
      val n = d.now
      DefaultWeek(n.weekOfWeekyear.get, n.year.get)
    }
  }
}
