name := "akka study"

version := "1.0"

scalaVersion := "2.10.2"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

EclipseKeys.withSource := true

resolvers ++= Seq(
	"Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
	"spray repo" at "http://repo.spray.io",
	"spray repo Nightly" at "http://nightlies.spray.io")

libraryDependencies ++= Seq(
	"com.typesafe.akka" %% "akka-actor" % "2.2.0",
	"com.typesafe.akka" %% "akka-slf4j" % "2.2.0",
	"com.typesafe.akka" %% "akka-testkit" % "2.2.0",
	"org.scalatest" %% "scalatest" % "1.9.1",
	"io.spray"          %  "spray-can"       % "1.2-20130822",
  "io.spray"          %  "spray-routing"   % "1.2-20130822",
  "io.spray"          %% "spray-json"      % "1.2.5")
