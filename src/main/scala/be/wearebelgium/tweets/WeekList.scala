package be.wearebelgium.tweets

trait Week {
  def number: Int
  def year: Int
  def startDate: DateTime
  def endDate: DateTime
  def spansYears: Boolean
  def spansMonths: Boolean
  def isBooked: Boolean = false
}

case class DefaultWeek(number: Int, year: Int = DateTime.now.year.get) extends Week {

  private[this] val maxWeek = new DateTime().withYear(year).week.getMaximumValue
  require(number <= maxWeek, "The week number must be smaller than " + maxWeek + " but is: " + number)

  lazy val startDate = DateTime.now.firstDateOfWeek.withYear(year).withWeek(number)
  lazy val endDate = startDate.lastDateOfWeek
  lazy val spansYears = startDate.year.get != endDate.year.get
  lazy val spansMonths = startDate.monthOfYear.get != endDate.monthOfYear.get
}

case class BookedWeek(private val week: Week, participant: Option[Participant]) extends Week {
  def number = week.number
  def startDate = week.startDate
  def endDate = week.endDate
  def spansYears = week.spansYears
  def spansMonths = week.spansMonths
  def year: Int = week.year

  override def isBooked: Boolean = participant.isDefined
}

object WeekList {
  def apply(startWeek: DefaultWeek = DefaultWeek(DateTime.now.week.get), maxItems: Int = 10) = new WeekList(startWeek, maxItems)
}
class WeekList(startWeek: Week, maxItems: Int) extends collection.TraversableProxy[Week] {

  val startDate = startWeek.startDate
  private val lastWeekOfYear = startDate.lastWeekOfYear

  val self: Traversable[Week] = {
    val extra = (1 until maxItems) map { idx ⇒
      val nwWk = startWeek.number + idx
      val overflows = nwWk > lastWeekOfYear
      DefaultWeek(if (overflows) nwWk - lastWeekOfYear else nwWk, if (overflows) startWeek.year + 1 else startWeek.year)
    }
    Seq(startWeek) ++ extra
  }

  def withBookings(coll: MongoCollection, dao: ParticipantDao): Seq[BookedWeek] = {
    val bookingsByYear = self.groupBy(_.year) map {
      case (year, weeks) ⇒
        val start = weeks.headOption.map(_.number).getOrElse(0)
        val end = weeks.lastOption.map(_.number).getOrElse(start)
        year -> bookedWeeks(dao, coll, year, start, end)
    }
    val bookings = bookingsByYear.values.flatten.toSeq
    val unbooked = self.filterNot(wk ⇒ bookings.exists(bk ⇒ bk.number == wk.number && bk.year == wk.year)) map { wk ⇒
      BookedWeek(wk, None)
    }
    val mixed = (unbooked ++ bookings)
    mixed.toList.sortWith((left, right) ⇒ if (left.year == right.year) left.number > right.number else left.year > right.year)
  }

  private def bookedWeeks(dao: ParticipantDao, coll: MongoCollection, year: Int, start: Int, end: Int) =
    (coll.find(bookingQuery(year, start, end)) map (bookedWeek(dao, _))).toSeq

  private def bookedWeek(dao: ParticipantDao, bk: DBObject) = {
    val wk = DefaultWeek(bk.as[Int]("number"), bk.as[Int]("year"))
    val ref = bk.getAs[ObjectId]("pId") flatMap dao.findOneById
    BookedWeek(wk, ref)
  }

  private def bookingQuery(year: Int, min: Int, max: Int) = MongoDBObject("year" -> year) ++ ("number" $gte min $lte max)

}

