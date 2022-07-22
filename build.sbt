import com.typesafe.config.ConfigFactory

import scala.sys.process._
import java.io.File
import scala.language.dynamics

val conf = ConfigFactory.parseFile(new File("src/main/resources/application.conf"))

lazy val mode = taskKey[String]("Mode")
lazy val curr_mode = taskKey[Map[String, Any]]("Modes")

mode := conf.getString("seiner.mode")
curr_mode := Map(
  "port.http" -> conf.getInt(s"seiner.modes.${mode.value}.port.http"),
  "port.ws" -> conf.getInt(s"seiner.modes.${mode.value}.port.ws"),
  "interface" -> conf.getString(s"seiner.modes.${mode.value}.interface"),
  "client-source" -> conf.getString(s"seiner.modes.${mode.value}.client-source"),
  "interactive" -> conf.getBoolean(s"seiner.modes.${mode.value}.interactive")
)

lazy val buildClient = taskKey[Unit]("Build client")
buildClient := {
  val s = streams.value

  val platform =
    if (sys.props("os.name").contains("Windows"))
      "windows"
    else
      "linux"

  val shell = platform match {
    case "windows" => Seq("cmd", "/c")
    case "linux"   => Seq("bash", "-c")
  }

  val pipeline = Map(
    "echo cd" -> s"echo ${curr_mode.value("client-source")}",
    "change dir" -> s"cd ${curr_mode.value("client-source")}",
    "echo" -> (platform match {
      case "windows" => "echo %cd%"
      case "linux"   => "pwd"
    }),
    "install" -> "npm install",
    // test -> "npm run test",
    // lint -> "npm run lint",
    "build" -> "npm run build"
  ).values.reduce((cmd1, cmd2) => s"$cmd1 && $cmd2")

  s.log.info("Building client...")

  if ((shell :+ pipeline !) == 0) {
    s.log.success("frontend build successful!")
  } else {
    throw new IllegalStateException("frontend build failed!")
  }
}

val deps = new {
  val akka = "2.6.8"
  val akkaHttp = "10.2.9"
}

ThisBuild / scalaVersion := "2.13.8"

ThisBuild / organization := conf.getString("seiner.build.organization")
ThisBuild / version := conf.getString("seiner.build.version")
ThisBuild / organizationName := conf.getString("seiner.build.organizationName")
ThisBuild / licenses := Seq("MIT" -> url("https://mit-license.org"))

ThisBuild / mainClass := Some(s"$organization.Program")

Compile / compile := (Compile / compile).dependsOn(buildClient).value //.withCachedResolution(true)
unmanagedResources / excludeFilter := {
  val client = ((Compile / resourceDirectory).value / "seiner-client").getCanonicalPath
  new SimpleFileFilter(_.getCanonicalPath startsWith client)
}

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
