package be.wearebelgium.tweets

import com.typesafe.config._
import java.io.File

class Settings {

  private val cfg = {
    val cfgFile = new File(sys.props("user.home") + "/wearebelgium.conf")
    val defaultConf = ConfigFactory.load()
    if (cfgFile.exists()) {
      ConfigFactory.parseFile(cfgFile).resolve().withFallback(defaultConf)
    } else defaultConf
  }

  val web = WebConfig(
    cfg.getString("wearebelgium.web.domain"),
    cfg.getInt("wearebelgium.web.port"),
    cfg.getString("wearebelgium.web.guiUrl"),
    sys.env.get("WAB_PUBLIC_URL").getOrElse(cfg.getString("wearebelgium.web.appUrl")))

  val mongo = MongoConfiguration(sys.env.get("MONGOLAB_URI").getOrElse(cfg.getString("wearebelgium.mongo.uri")))

  val twitter = TwitterConfig(
    sys.env.get("TWITTER_CLIENT_ID") getOrElse cfg.getString("wearebelgium.oauth.twitter.clientId"),
    sys.env.get("TWITTER_CLIENT_SECRET") getOrElse cfg.getString("wearebelgium.oauth.twitter.clientSecret"),
    sys.env.get("TWITTER_ACCESS_TOKEN") getOrElse cfg.getString("wearebelgium.oauth.twitter.accessToken"),
    sys.env.get("TWITTER_ACCESS_SECRET") getOrElse cfg.getString("wearebelgium.oauth.twitter.accessSecret"))
}
