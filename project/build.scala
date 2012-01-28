import sbt._
object MyApp extends Build
{
	lazy val root = Project("root", file(".")) dependsOn(unfilteredScalate)
	lazy val unfilteredScalate = uri("http://github.com/unfiltered/unfiltered-scalate")
}
