scalacOptions += "-deprecation"

resolvers += "Jawsy.fi M2 releases" at "http://oss.jawsy.fi/maven2/releases"

resolvers += Classpaths.typesafeResolver

libraryDependencies <+= sbtVersion(v => "com.github.siasia" %% "xsbt-web-plugin" % (v+"-0.2.11.1"))

addSbtPlugin("fi.jawsy.sbtplugins" %% "sbt-jrebel-plugin" % "0.9.0")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.1.2")

addSbtPlugin("com.mojolly.scalate" % "xsbt-scalate-generator" % "0.2.1")

libraryDependencies += "net.liftweb" % "lift-json_2.9.1" % "2.4"

addSbtPlugin("com.bowlingx" % "xsbt-wro4j-plugin" % "0.1.0-SNAPSHOT")
