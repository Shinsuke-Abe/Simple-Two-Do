import com.typesafe.startscript.StartScriptPlugin

seq(StartScriptPlugin.startScriptForClassesSettings: _*)

organization := "com.example"

name := "maosandbox"

scalaVersion := "2.9.1"

version := "0.1.0-SNAPSHOT"

libraryDependencies ++= Seq(
   "net.databinder" %% "unfiltered-filter" % "0.5.1",
   "net.databinder" %% "unfiltered-jetty" % "0.5.1"
)

resolvers ++= Nil
