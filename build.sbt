import com.typesafe.config.ConfigFactory

import java.io.File

val deps = new {
  val akka = "2.6.8"
  val akkaHttp = "10.2.9"
}

val conf = ConfigFactory.parseFile(new File("src/main/resources/application.conf"))

ThisBuild / scalaVersion := "2.13.8"

ThisBuild / organization := conf.getString("seiner.build.organization")
ThisBuild / version := conf.getString("seiner.build.version")
ThisBuild / organizationName := conf.getString("seiner.build.organizationName")
ThisBuild / licenses := Seq("MIT" -> url("https://mit-license.org"))

ThisBuild / mainClass := Some(s"$organization.Program")

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "seiner",
    libraryDependencies := Seq(
      // config parsing
      "com.typesafe" % "config" % "1.4.2",

      // nscala time
      "com.github.nscala-time" %% "nscala-time" % "2.30.0",

      // argument processing
      "org.rogach" %% "scallop" % "4.1.0",
      "com.github.scopt" %% "scopt" % "4.0.1",

      // akka typed
      "com.typesafe.akka" %% "akka-actor-typed" % deps.akka,

      // akka stream
      "com.typesafe.akka" %% "akka-stream" % deps.akka,
      "com.typesafe.akka" %% "akka-stream-testkit" % deps.akka,

      // akka http
      "com.typesafe.akka" %% "akka-http-core" % deps.akkaHttp,
      "com.typesafe.akka" %% "akka-http-testkit" % deps.akkaHttp,
      "com.typesafe.akka" %% "akka-http-spray-json" % deps.akkaHttp
    )
  )
