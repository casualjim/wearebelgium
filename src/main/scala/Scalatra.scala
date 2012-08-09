import be.wearebelgium.tweets._
import org.scalatra._
import javax.servlet.ServletContext
import be.wearebelgium.tweets.MongoConfiguration

class Scalatra extends LifeCycle {
  val mongoUrl = {
    sys.env.get("MONGOLAB_URI") getOrElse "mongodb://test:test@127.0.0.1:27017/wearebelgium_dev"
  }

  val clientId = {
    sys.env.get("TWITTER_CLIENT_ID") getOrElse "VTULrf6vcx1yfUeIDC0Ag"
  }

  val clientSecret = {
    sys.env.get("TWITTER_CLIENT_SECRET") getOrElse "aBDGYbqwLSArMm7UAKHXmWAi1oK5LSXYYQcMyxUfb0"
  }

  val appAccessToken = {
    sys.env.get("TWITTER_ACCESS_TOKEN") getOrElse "626004573-7bftQcKE2fGTIbUZwlGIHQWMw06V9vPKQcbR797S"
  }

  val appAccessSecret = {
    sys.env.get("TWITTER_ACCESS_SECRET") getOrElse "KqkMuFFKAVLxq64q6lFvQjJ7dyGxkvz8W6jxvoinPo"
  }

  override def init(context: ServletContext) {
    val home = context.addServlet(classOf[HomeServlet].getName, new HomeServlet)
    home.addMapping("/*")
    home.setInitParameter("clientId", clientId)
    home.setInitParameter("clientSecret", clientSecret)
    home.setInitParameter("appAccessToken", appAccessToken)
    home.setInitParameter("appAccessSecret", appAccessSecret)

    context("mongoConfig") = MongoConfiguration(mongoUrl)
  }
}
