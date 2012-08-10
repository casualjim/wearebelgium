import scala.xml.Group
import com.typesafe.startscript.StartScriptPlugin
import scalariform.formatter.preferences._
import StartScriptPlugin._
import ScalateKeys._
import Wro4jKeys._
import net.liftweb.json._
import JsonDSL._

organization := "be.wearebelgium"

name := "tweets"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.9.2"

seq(webSettings :_*)

classpathTypes ~= (_ + "orbit")

libraryDependencies ++= Seq(
  "org.scalatra" % "scalatra" % "2.2.0-SNAPSHOT",
  "org.scalatra" % "scalatra-scalate" % "2.2.0-SNAPSHOT",  
  "org.scalatra" % "scalatra-lift-json" % "2.2.0-SNAPSHOT",
  "org.scalatra" % "contrib-commons" % "1.1.0-SNAPSHOT",
  "com.novus" %% "salat" % "1.9.0",
  "io.backchat.inflector"  %% "scala-inflector"    % "1.3.3",
  "commons-codec"            % "commons-codec"          % "1.6",
  "net.databinder.dispatch" %% "core" % "0.9.0",
  "org.scala-tools.time"     % "time_2.9.1"             % "0.5",
  "junit"                    % "junit"                  % "4.10"                % "test",
  "org.scalatra" % "scalatra-specs2" % "2.2.0-SNAPSHOT" % "test",
  "ch.qos.logback" % "logback-classic" % "1.0.6" % "runtime",
  "org.eclipse.jetty" % "jetty-webapp" % "8.1.5.v20120716" % "container",
  "org.eclipse.jetty" % "test-jetty-servlet" % "8.1.5.v20120716" % "test",
  "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container;provided;test" artifacts (Artifact("javax.servlet", "jar", "jar"))
)

resolvers += "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

testOptions += Tests.Argument(TestFrameworks.Specs2, "console", "junitxml")

testOptions <+= (crossTarget map { ct =>
 Tests.Setup { () => System.setProperty("specs2.junit.outDir", new File(ct, "specs-reports").getAbsolutePath) }
})

seq(jrebelSettings: _*)

jrebel.webLinks <+= (sourceDirectory in Compile)(_ / "webapp")

homepage := Some(url("https://github.com/scalatra/oauth2-server"))

startYear := Some(2010)

licenses := Seq(("MIT", url("https://github.com/scalatra/oauth2-server/raw/HEAD/LICENSE")))

pomExtra <<= (pomExtra, name, description) {(pom, name, desc) => pom ++ Group(
  <scm>
    <connection>scm:git:git://github.com/scalatra/oauth2-server.git</connection>
    <developerConnection>scm:git:git@github.com:scalatra/oauth2-server.git</developerConnection>
    <url>https://github.com/scalatra/oauth2-server</url>
  </scm>
  <developers>
    <developer>
      <id>casualjim</id>
      <name>Ivan Porto Carrero</name>
      <url>http://flanders.co.nz/</url>
    </developer>
  </developers>
)}

packageOptions <+= (name, version, organization) map {
    (title, version, vendor) =>
      Package.ManifestAttributes(
        "Created-By" -> "Simple Build Tool",
        "Built-By" -> System.getProperty("user.name"),
        "Build-Jdk" -> System.getProperty("java.version"),
        "Specification-Title" -> title,
        "Specification-Version" -> version,
        "Specification-Vendor" -> vendor,
        "Implementation-Title" -> title,
        "Implementation-Version" -> version,
        "Implementation-Vendor-Id" -> vendor,
        "Implementation-Vendor" -> vendor
      )
  }

publishMavenStyle := true

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

pomIncludeRepository := { x => false }

seq(scalariformSettings: _*)

ScalariformKeys.preferences :=
  (FormattingPreferences()
        setPreference(IndentSpaces, 2)
        setPreference(AlignParameters, false)
        setPreference(AlignSingleLineCaseStatements, true)
        setPreference(DoubleIndentClassDeclaration, true)
        setPreference(RewriteArrowSymbols, true)
        setPreference(PreserveSpaceBeforeArguments, true)
        setPreference(IndentWithTabs, false))

(excludeFilter in ScalariformKeys.format) <<= excludeFilter(_ || "*Spec.scala")

seq(scalateSettings:_*)

scalateTemplateDirectory in Compile <<= (baseDirectory) { _ / "src/main/webapp/WEB-INF" }

scalateImports ++= Seq(
  "import be.wearebelgium.tweets._"
)

scalateBindings ++= Seq(
  Binding("flash", "scala.collection.Map[String, Any]", defaultValue = "Map.empty"),
  Binding("session", "javax.servlet.http.HttpSession"),
  Binding("sessionOption", "Option[javax.servlet.http.HttpSession]"),
  Binding("params", "scala.collection.Map[String, String]"),
  Binding("multiParams", "org.scalatra.MultiParams"),
  Binding("user", "Participant", defaultValue = "null"),
  Binding("userOption", "Option[Participant]", defaultValue = "null"),
  Binding("isAuthenticated", "Boolean", defaultValue = "false"),
  Binding("isAnonymous", "Boolean", defaultValue = "true"))

seq(buildInfoSettings: _*)

sourceGenerators in Compile <+= buildInfo

buildInfoKeys := Seq[Scoped](name, version, scalaVersion, sbtVersion)

buildInfoPackage := "be.wearebelgium"

seq(startScriptForWarSettings: _*)

startScriptJettyVersion in Compile := "8.1.5.v20120716"

startScriptJettyChecksum in Compile := "5cac2f735d5ca34b3ee6e04ef932d2dbd962fdc5"

startScriptJettyURL in Compile <<= (startScriptJettyVersion in Compile) { (version) => "http://download.eclipse.org/jetty/" + version + "/dist/jetty-distribution-" + version + ".zip" }

watchSources <++= (sourceDirectory in Compile) map (d => (d / "webapp" ** "*").get)

seq(wro4jSettings: _*)

compile in Compile <<= (compile in Compile).dependsOn(generateResources in Compile)

(webappResources in Compile) <+= (targetFolder in generateResources in Compile)

outputFolder in (Compile, generateResources) := "assets/"

processorProvider in (Compile, generateResources) := new OAuth2Processors

wroFile in (Compile, generateResources) <<= (baseDirectory)(_ / "project" / "wro.xml")

propertiesFile in (Compile, generateResources) <<= (baseDirectory)(_ / "project" / "wro.properties")

seq(pomGenSettings:_*)
