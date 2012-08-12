package be.wearebelgium.tweets

import org.scalatra._
import extension.TypedParamSupport
import org.scalatra.liftjson._
import scalate.ScalateSupport
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.io.PrintWriter
import org.scalatra.scalate.ScalatraRenderContext
import javax.servlet.ServletConfig
import org.slf4j.LoggerFactory
import org.scalatra.ServiceUnavailable
import com.ning.http.client.oauth.RequestToken
import be.wearebelgium.util.Logging

class HomeServlet extends ScalatraServlet with Logging with ScalateSupport with SessionSupport with FlashMapSupport with LiftJsonSupport with TypedParamSupport {

  var twitterProvider: OAuthProvider = null
  var twitterApi: TwitterApiCalls = null

  def settings = servletContext("wearebelgium.settings").asInstanceOf[Settings]
  def mongoConfig = settings.mongo
  var participantDao: ParticipantDao = null
  var bookings: MongoCollection = null

  override def initialize(config: ServletConfig) {
    super.initialize(config)
    twitterProvider = OAuthProvider("twitter", settings.twitter.clientId, settings.twitter.clientSecret, Nil)
    twitterApi = new TwitterApiCalls(OAuthToken(settings.twitter.accessToken, settings.twitter.accessSecret), twitterProvider, callbackUrl)
    participantDao = new ParticipantDao(mongoConfig.db("participants"))
    bookings = mongoConfig.db("bookings")
    bookings.ensureIndex(Map("number" -> 1, "year" -> 1), "bookings_number_year_idx", unique = true)
    bookings.ensureIndex("pId")
  }

  def callbackUrl = {
    val pub = settings.web.guiUrl
    pub + urlWithContextPath("auth/twitter/callback")
  }

  private[this] def urlWithContextPath(path: String, params: Iterable[(String, Any)] = Iterable.empty): String = {
    val newPath = path match {
      case x if x.startsWith("/") ⇒ ensureSlash(contextPath) + ensureSlash(path)
      case _                      ⇒ ensureSlash(path)
    }
    val pairs = params map { case (key, value) ⇒ key.urlEncode + "=" + value.toString.urlEncode }
    val queryString = if (pairs.isEmpty) "" else pairs.mkString("?", "&", "")
    logger.debug("The url with context path: %s" format newPath)
    newPath + queryString
  }

  private def ensureSlash(candidate: String) = {
    (candidate.startsWith("/"), candidate.endsWith("/")) match {
      case (true, true)   ⇒ candidate.dropRight(1)
      case (true, false)  ⇒ candidate
      case (false, true)  ⇒ "/" + candidate.dropRight(1)
      case (false, false) ⇒ "/" + candidate
    }
  }

  before() {
    contentType = "text/html"
  }

  get("/") {
    jade("home", "title" -> "Welcome", "weeks" -> weeks)
  }

  get("/auth/twitter") {
    twitterApi.fetchRequestToken().fold(
      err ⇒ ServiceUnavailable(err),
      tok ⇒ {
        session("requestToken") = tok
        redirect(twitterApi.signedAuthorize(tok))
      })
  }

  get("/auth/twitter/callback") {
    val reqToken = session.get("requestToken").map(_.asInstanceOf[RequestToken]).orNull

    if (reqToken == null) {
      user = null
      flash("error") = "Unexpected state, start over and try again"
      redirect("/")
    } else {
      val accessToken = twitterApi.fetchAccessToken(reqToken, params("oauth_verifier"))()
      accessToken.fold(
        err ⇒ {
          flash("error") = err
          redirect("/")
        },
        tok ⇒ {
          val userProfile = twitterApi.withAccessToken(OAuthToken(tok.getKey, tok.getSecret)).getProfile()
          val id = (userProfile \ "id").extract[Long]
          val part = participantDao.findOne(Map("twitterId" -> id)) getOrElse {
            val p = Participant(
              id,
              (userProfile \ "screen_name").extract[String],
              (userProfile \ "name").extract[String],
              (userProfile \ "description").extract[String],
              (userProfile \ "profile_image_url").extract[String],
              tok.getKey,
              tok.getSecret)
            participantDao.save(p, WriteConcern.Safe)
            p
          }
          user = part
          flash("success") = "Welcome, " + part.name.blankOption.getOrElse(part.screenName)
          redirect("/")
        })
    }
  }

  post("/book/:year/:number") {
    needsAuth()
    val year = params("year").toInt
    val number = params("number").toInt
    val week = DefaultWeek(number, year)
    if (!bookings.exists(forWeek(week))) {
      bookings.save(Map("number" -> number, "year" -> year, "pId" -> user.id))
      redirect("/")
    } else {
      val msg = "The week %s in year %s is already booked.".format(number, year)
      flash("error") = msg
      redirect("/")
    }
  }

  post("/update") {
    needsAuth()
    twitterApi.clientFor(user).postUpdate(params("update_text")).fold(
      err ⇒ ServiceUnavailable("Posting the tweet failed because: " + err.getMessage),
      tweet ⇒ {
        // TODO: track this tweet?
        flash("success") = "Tweet posted!"
        redirect("/")
      })
  }

  private def needsAuth() {
    if (isAnonymous) {
      flash("error") = "You need to be signed in to access that functionality."
      redirect("/")
    }
  }

  private def weeks = {
    val starting = params.getAs[Int]("from") getOrElse DateTime.now.week.get
    val maxItems = params.getAs[Int]("pageSize") getOrElse 10
    WeekList(DefaultWeek(starting), maxItems).withBookings(bookings, participantDao)
  }

  private def forWeek(week: Week)(dbo: DBObject) =
    dbo.getAs[Int]("number") == Some(week.number) && dbo.getAs[Int]("year") == Some(week.year)

  def user: Participant = userOption.orNull
  def user_=(u: Participant) = session("user") = u
  def userOption: Option[Participant] = session.get("user").map(_.asInstanceOf[Participant])
  def isAuthenticated = userOption.isDefined
  def isAnonymous = userOption.isEmpty

  get("/logout") {
    session.invalidate()
    redirect("/")
  }

  notFound {
    contentType = null
    serveStaticResource() getOrElse resourceNotFound()
  }

  override protected def createRenderContext(req: HttpServletRequest, resp: HttpServletResponse, out: PrintWriter) = {
    val ctx = super.createRenderContext(req, resp, out).asInstanceOf[ScalatraRenderContext]
    ctx.attributes.update("session", ctx.session)
    ctx.attributes.update("sessionOption", ctx.sessionOption)
    ctx.attributes.update("flash", ctx.flash)
    ctx.attributes.update("params", ctx.params)
    ctx.attributes.update("multiParams", ctx.multiParams)
    ctx.attributes.update("user", user)
    ctx.attributes.update("userOption", userOption)
    ctx.attributes.update("isAuthenticated", isAuthenticated)
    ctx.attributes.update("isAnonymous", isAnonymous)
    ctx
  }

}
