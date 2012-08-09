package be.wearebelgium.tweets

import org.scalatra.test.specs2._
import org.eclipse.jetty.servlet.ServletHolder

// For more on Specs2, see http://etorreborre.github.com/specs2/guide/org.specs2.guide.QuickStart.html 
class HomeServletSpec extends ScalatraSpec { def is =
  "GET / on HomeServlet"                     ^
    "should return status 200"                  ! root200^
                                                end
   
                                                
  val app = new ServletHolder(new HomeServlet)
  app.setInitParameter("clientId", "blah")
  app.setInitParameter("clientSecret", "blahsecret")
  servletContextHandler.addServlet(app, "/*")

  def root200 = get("/") { 
    status must_== 200
  }
}
