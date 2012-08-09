import be.wearebelgium.tweets._
import org.scalatra._
import javax.servlet.ServletContext

class Scalatra extends LifeCycle {
  override def init(context: ServletContext) {
    val home = context.addServlet(classOf[HomeServlet].getName(), new HomeServlet)
    home.addMapping("/*")
    home.setInitParameter("clientId", "VTULrf6vcx1yfUeIDC0Ag")
    home.setInitParameter("clientSecret", "aBDGYbqwLSArMm7UAKHXmWAi1oK5LSXYYQcMyxUfb0")
    home.setInitParameter("appAccessToken", "626004573-7bftQcKE2fGTIbUZwlGIHQWMw06V9vPKQcbR797S")
    home.setInitParameter("appAccessSecret", "KqkMuFFKAVLxq64q6lFvQjJ7dyGxkvz8W6jxvoinPo")
  }
}
