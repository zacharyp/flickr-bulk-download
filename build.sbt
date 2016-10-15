import sbt.Keys._

organization := "org.zachary.flickr"
name := "flickr-downloader"
version := "1.0"

val scalaV = "2.11.8"
val akkaVersion = "2.4.11"

lazy val Akka = Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion
)

lazy val flickrDownloader = project.in(file("."))
  .settings(
    scalaVersion := scalaV,
    mainClass in(Compile, run) := Some("org.zachary.flickr.Main")
  )
  .settings(libraryDependencies ++=
    Akka ++ Seq(
      "com.typesafe" % "config" % "1.3.1",
      "commons-io" % "commons-io" % "1.3.2",
      "org.json4s" %% "json4s-jackson" % "3.3.0",
      "org.json4s" %% "json4s-native" % "3.3.0",
      "com.flickr4java" % "flickr4java" % "2.16"
    )
  )
