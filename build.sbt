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
   "org.fusesource.scalate" % "scalate-util" % "1.5.3" % "test"
)

resolvers ++= Nil
