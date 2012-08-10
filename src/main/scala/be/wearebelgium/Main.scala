package be.wearebelgium

import org.eclipse.jetty.webapp.WebAppContext
import org.eclipse.jetty.server.nio.SelectChannelConnector
import org.eclipse.jetty.server.Server

object Main {

  def main(args: Array[String]) = {
    val server: Server = new Server

    server setGracefulShutdown 5000
    server setSendServerVersion false
    server setSendDateHeader true
    server setStopAtShutdown true

    val connector = new SelectChannelConnector
    connector setPort sys.env.get("PORT").map(_.toInt).getOrElse(8080)
    connector setMaxIdleTime 90000
    server addConnector connector

    val webapp = "target/webapp"
    val webApp = new WebAppContext
    webApp setContextPath "/"
    webApp setResourceBase webapp
    webApp setDescriptor (webapp + "/WEB-INF/web.xml")

    server setHandler webApp

    server.start()
  }
}
