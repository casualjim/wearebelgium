import be.wearebelgium.tweets._
import org.scalatra._
import javax.servlet.ServletContext
import be.wearebelgium.tweets.MongoConfiguration

class Scalatra extends LifeCycle {

  val settings = new Settings

  override def init(context: ServletContext) {
    context.mount(new HomeServlet, "/*")
    context("wearebelgium.settings") = settings
  }
}
