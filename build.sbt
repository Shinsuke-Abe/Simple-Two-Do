import com.typesafe.startscript.StartScriptPlugin

seq(StartScriptPlugin.startScriptForClassesSettings: _*)

organization := "com.example"

name := "maosandbox"

scalaVersion := "2.9.1"

version := "0.1.0-SNAPSHOT"

libraryDependencies ++= Seq(
   "net.databinder" %% "unfiltered-filter" % "0.5.3",
   "net.databinder" %% "unfiltered-jetty" % "0.5.3",
   "org.fusesource.scalate" % "scalate-core" % "1.5.3",
   "org.fusesource.scalate" % "scalate-util" % "1.5.3" % "test",
   "org.twitter4j" % "twitter4j-core" % "[2.2,)",
   "net.databinder" %% "unfiltered-scalatest" % "0.5.3",
   "com.mongodb.casbah" %% "casbah" % "2.1.5-1",
   "com.novus" %% "salat-core" % "0.0.8-SNAPSHOT"
)

resolvers += "twitter4j.org Repository" at "http://twitter4j.org/maven2"

resolvers += "repo.novus for salat build release" at "http://repo.novus.com/releases/"

resolvers += "repo.novus for salat build snapshot" at "http://repo.novus.com/snapshots/"
