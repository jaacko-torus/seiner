package com.jaackotorus.seiner

import com.typesafe.config
import scopt.OParser

import java.net.URI
import scala.util.{Failure, Success, Try}

object Program {
  def main(args: Array[String]): Unit = {
    val builder = OParser.builder[CLIConfig]

    val parser = {
      import builder._
      OParser.sequence(
        programName("driver"),
        head("driver", "0.1.0"),
        version('v', "version"),
        help('h', "help"),
        opt[Int]("port-http")
          .action((port_http, c) => c.copy(port_http = port_http))
          .text(
            s"(default: ${env.port.http}) port for the client HTTP service (e.x.: 80, 8080, 9000)"
          ),
        opt[Int]("port-ws")
          .action((port_ws, c) => c.copy(port_ws = port_ws))
          .text(s"(default: ${env.port.ws}) port for the client WS service"),
        opt[String]("interface")
          .abbr("i")
          .action((interface, c) => c.copy(interface = interface))
          .validate(interface =>
            if (isValidIpV4(interface)) {
              success
            } else {
              failure(
                s"Interface \"$interface\" is not a valid a valid interface address"
              )
            }
          )
          .text(
            s"(default: ${env.interface}) interface address (e.x.: 0.0.0.0, localhost, 127.0.0.1)"
          ),
        opt[String]("client-source")
          .abbr("cs")
          .action((client_source, c) => c.copy(client_source = client_source))
          .validate(client_source =>
            if (isValidURI(client_source)) {
              success
            } else {
              failure(s"Client source \"$client_source\" is not a valid URI")
            }
          )
          .text(
            s"(default: ${env.client_source}) directory to be considered the client root. It should have an `index.html` file inside"
          ),
        opt[Boolean]("interactive")
          .action((interactive, c) => c.copy(interactive = interactive))
          .text(s"(default: ${env.interactive}) server in interactive mode"),
        opt[Unit]("local")
          .action((_, c) => {
            val env = conf.local
            c.copy(
              local = true,
              port_http = env.port.http,
              port_ws = env.port.ws,
              interface = env.interface,
              client_source = env.client_source,
              interactive = env.interactive
            )
          })
          .text(s"(default: false) use local settings")
      )
    }

    OParser.parse(parser, args, CLIConfig()) match {
      case Some(config) =>
        Server.run(config)
      case _ =>
    }
  }

  def isValidURI(string: String): Boolean = {
    Try(new URI(string)) match {
      case Success(_) => true
      case Failure(_) => false
    }
  }

  def isValidIpV4(string: String): Boolean = {
    val n = raw"((1?)(\d?)\d|2[0-4]\d|25[0-5])".r
    (raw"($n\.){3}$n|localhost").r.matches(string)
  }

  case class CLIConfig(
      local: Boolean = false,
      port_http: Int = env.port.http,
      port_ws: Int = env.port.ws,
      interface: String = env.interface,
      client_source: String = env.client_source,
      interactive: Boolean = env.interactive
  )

  object conf {
    private val app_conf: config.Config = config.ConfigFactory.load("application")

    case class Port(
        http: Int,
        ws: Int
    )

    case class Environment(
        port: Port,
        interface: String,
        client_source: String,
        interactive: Boolean
    )

    val local = Environment(
      port = Port(
        http = app_conf.getInt("seiner.local.port.http"),
        ws = app_conf.getInt("seiner.local.port.ws")
      ),
      interface = app_conf.getString("seiner.local.interface"),
      client_source = app_conf.getString("seiner.local.client-source"),
      interactive = app_conf.getBoolean("seiner.local.interactive")
    )

    val deploy = Environment(
      port = Port(
        http = app_conf.getInt("seiner.deploy.port.http"),
        ws = app_conf.getInt("seiner.deploy.port.ws")
      ),
      interface = app_conf.getString("seiner.deploy.interface"),
      client_source = app_conf.getString("seiner.deploy.client-source"),
      interactive = app_conf.getBoolean("seiner.deploy.interactive")
    )
  }

  val env = conf.deploy
}
