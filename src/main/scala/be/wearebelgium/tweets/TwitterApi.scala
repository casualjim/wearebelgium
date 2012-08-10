package be.wearebelgium.tweets

import com.ning.http.client.oauth.{ RequestToken, ConsumerKey }
import dispatch._
import dispatch.oauth._
import net.liftweb.json._

case class OAuthToken(token: String, secret: String)

case class OAuthProvider(name: String, clientId: String, clientSecret: String, scope: List[String] = Nil)

object read {
  val JValue = dispatch.as.String.andThen(parse)
}

trait TwitterApiUrls extends oauth.SomeEndpoints {
  val requestToken: String = "https://api.twitter.com/oauth/request_token"

  val accessToken: String = "https://api.twitter.com/oauth/access_token"

  val authorize: String = "https://api.twitter.com/oauth/authenticate"
}

class TwitterApiCalls(userToken: OAuthToken, provider: OAuthProvider, val callback: String)(implicit formats: Formats)
    extends oauth.SomeHttp with oauth.SomeCallback with oauth.SomeConsumer with oauth.Exchange with TwitterApiUrls {

  val consumer: ConsumerKey = new ConsumerKey(provider.clientId, provider.clientSecret)

  val http: Executor = dispatch.Http

  private def token = new RequestToken(userToken.token, userToken.secret)

  private val urlBase = :/("api.twitter.com").secure / "1"

  def getProfile(id: Option[Int] = None): JValue = {
    val u = urlBase / "account" / "verify_credentials.json"
    http(u <@ (consumer, token) OK read.JValue)()
  }

  def withAccessToken(userToken: OAuthToken) = new TwitterApiCalls(userToken, provider, callback)
  def clientFor(participant: Participant) = withAccessToken(participant.token)

  def postUpdate(text: String) = {
    http(urlBase / "statuses" / "update.json" << Map("status" -> text.urlEncode) OK read.JValue).either()
  }
}