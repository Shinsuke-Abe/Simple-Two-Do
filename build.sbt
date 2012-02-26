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
   "com.mongodb.casbah" % "casbah_2.8.1" % "2.0.1"
)

resolvers += "twitter4j.org Repository" at "http://twitter4j.org/maven2"
