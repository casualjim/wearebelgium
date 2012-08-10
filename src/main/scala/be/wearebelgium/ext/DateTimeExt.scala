package be.wearebelgium.ext

import org.scala_tools.time.Imports._
import org.joda.time.MutableDateTime

class DateTimeExt(d: DateTime) {

  def firstDayOfMonth = d.dayOfMonth.getMinimumValue
  def firstDateOfMonth = getFirstLastDate(firstDayOfMonth)

  def lastWeekOfYearDate = {
    val db = new MutableDateTime()
    db.setYear(d.year.get)
    db.setMonthOfYear(d.monthOfYear.getMaximumValue)
    db.setDayOfMonth(31)
    db.toDateTime
  }
  def lastWeekOfYear = d.weekOfWeekyear.getMaximumValue
  def firstWeekOfYear = 1

  private[this] def getFirstLastDate(day: Int) = {
    val db = new MutableDateTime()
    db.setYear(d.year.get)
    db.setMonthOfYear(d.monthOfYear.get)
    db.setDayOfMonth(day)
    db.toDateTime
  }

  def lastDateOfMonth = getFirstLastDate(lastDayOfMonth)
  def lastDayOfMonth = d.dayOfMonth().getMaximumValue
  def firstDateOfWeek = d - (d.dayOfWeek().get - 1).days
  def lastDateOfWeek = firstDateOfWeek + 6.days

}
