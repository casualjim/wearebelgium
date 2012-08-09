package be.wearebelgium.tweets

import org.scalatra._
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
import org.scalatra.InternalServerError

class HomeServlet extends ScalatraServlet with ScalateSupport with LiftJsonSupport {

  var twitterProvider: OAuthProvider = null
  var twitterApi: TwitterApiCalls = null
  private[this] val logger = LoggerFactory.getLogger(getClass)

  def mongoConfig = servletContext("mongoConfig").asInstanceOf[MongoConfiguration]

  override def initialize(config: ServletConfig) {
    super.initialize(config)
    (config.getInitParameter("clientId").blankOption, config.getInitParameter("clientSecret").blankOption) match {
      case (Some(clientId), Some(clientSecret)) ⇒
        twitterProvider = OAuthProvider("twitter", clientId, clientSecret, Nil)
        twitterApi = new TwitterApiCalls(twitterProvider, callbackUrl(config))
      case _ ⇒ throw new RuntimeException("client id and secret need to be configured")
    }
  }

  def callbackUrl(config: ServletConfig) = {
    val pub = config.getInitParameter("publicUrl").blankOption getOrElse "http://test.flanders.co.nz:8080"
    pub + urlWithContextPath("/auth/twitter/callback")
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

  get("/") {
    contentType = "text/html"
    jade("home", "title" -> "Welcome")
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
      InternalServerError("Unexpected state, start over and try again")
    } else {
      val accessToken = twitterApi.fetchAccessToken(reqToken, params("verifier"))()
      accessToken.fold(
        err ⇒ InternalServerError(err),
        tok ⇒ {
          twitterApi.userToken = OAuthToken(tok.getKey(), tok.getSecret())
          val userProfile = twitterApi.getProfile()

          // Fetch user
          redirect("/")
        })
    }
  }

  notFound {
    serveStaticResource() getOrElse resourceNotFound()
  }

  override protected def createRenderContext(req: HttpServletRequest, resp: HttpServletResponse, out: PrintWriter) = {
    val ctx = super.createRenderContext(req, resp, out).asInstanceOf[ScalatraRenderContext]
    ctx.attributes.update("session", ctx.session)
    ctx.attributes.update("sessionOption", ctx.sessionOption)
    ctx.attributes.update("flash", ctx.flash)
    ctx.attributes.update("params", ctx.params)
    ctx.attributes.update("multiParams", ctx.multiParams)
    ctx
  }

}
