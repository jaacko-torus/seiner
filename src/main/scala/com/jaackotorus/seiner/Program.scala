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
        programName("seiner"),
        head("seiner", "0.1.0"),
        version('v', "version"),
        help('h', "help"),
        opt[String]("mode")
          .action((mode, c) => {
            curr_mode = conf.mode match {
              case "dev"      => conf.modes.dev
              case "prod" | _ => conf.modes.prod
            }

            c.copy(
              mode = mode,
              port_http = curr_mode.port.http,
              port_ws = curr_mode.port.ws,
              interface = curr_mode.interface,
              client_source = curr_mode.client_source,
              interactive = curr_mode.interactive
            )
          })
          .text(s"(default: false) use local settings"),
        opt[Int]("port-http")
          .action((port_http, c) => c.copy(port_http = port_http))
          .text(
            s"(default: ${curr_mode.port.http}) port for the client HTTP service (e.x.: 80, 8080, 9000)"
          ),
        opt[Int]("port-ws")
          .action((port_ws, c) => c.copy(port_ws = port_ws))
          .text(s"(default: ${curr_mode.port.ws}) port for the client WS service"),
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
            s"(default: ${curr_mode.interface}) interface address (e.x.: 0.0.0.0, localhost, 127.0.0.1)"
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
            s"(default: ${curr_mode.client_source}) directory to be considered the client root. It should have an `index.html` file inside"
          ),
        opt[Boolean]("interactive")
          .action((interactive, c) => c.copy(interactive = interactive))
          .text(s"(default: ${curr_mode.interactive}) server in interactive mode")
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
      mode: String = conf.mode,
      port_http: Int = curr_mode.port.http,
      port_ws: Int = curr_mode.port.ws,
      interface: String = curr_mode.interface,
      client_source: String = curr_mode.client_source,
      interactive: Boolean = curr_mode.interactive
  )

  object conf {
    private val app_conf: config.Config = config.ConfigFactory.load("application")

    case class Port(
        http: Int,
        ws: Int
    )

    case class Mode(
        port: Port,
        interface: String,
        client_source: String,
        interactive: Boolean
    )

    val mode = app_conf.getString("seiner.mode")

    object modes {
      val dev = Mode(
        port = Port(
          http = app_conf.getInt("seiner.modes.dev.port.http"),
          ws = app_conf.getInt("seiner.modes.dev.port.ws")
        ),
        interface = app_conf.getString("seiner.modes.dev.interface"),
        client_source = app_conf.getString("seiner.modes.dev.client-source"),
        interactive = app_conf.getBoolean("seiner.modes.dev.interactive")
      )

      val prod = Mode(
        port = Port(
          http = app_conf.getInt("seiner.modes.prod.port.http"),
          ws = app_conf.getInt("seiner.modes.prod.port.ws")
        ),
        interface = app_conf.getString("seiner.modes.prod.interface"),
        client_source = app_conf.getString("seiner.modes.prod.client-source"),
        interactive = app_conf.getBoolean("seiner.modes.prod.interactive")
      )
    }
  }

  var curr_mode = conf.mode match {
    case "dev"  => conf.modes.dev
    case "prod" => conf.modes.prod
  }
}
