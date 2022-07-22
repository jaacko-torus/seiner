package com.jaackotorus.seiner
package service

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}

import scala.annotation.unused
import scala.concurrent.{ExecutionContextExecutor, Future}

object HTTP extends ServiceTrait[Unit, HTTP] {
  import Directives._

  def `routeGenerator+clientDir`(
      clientDir: String
  )(@unused value: Unit): Route = {
    get {
      (pathEndOrSingleSlash & redirectToTrailingSlashIfMissing(StatusCodes.TemporaryRedirect)) {
        getFromFile(s"$clientDir/dist/index.html")
      } ~ {
        getFromDirectory(s"$clientDir/dist")
      }
    } ~ pathPrefix("client") {
      // get {
      getFromBrowseableDirectory(clientDir)
      // }
    } ~ pathPrefix("resources") {
      // get {
      getFromBrowseableDirectory(s"$clientDir/..")
      // }
    }
  }

  def apply(
      interface: String,
      port: Int,
      routeGenerator: Unit => Route
  ): HTTP = new HTTP(interface, port, routeGenerator)
}

class HTTP(
    interface: String,
    port: Int,
    route: Unit => Route
) extends Service[Unit](interface, port, route)
    with Directives {
  implicit val system: ActorSystem = ActorSystem("HTTPServiceSystem")
  implicit val context: ExecutionContextExecutor = system.dispatcher
  def start: (HTTP, Future[Http.ServerBinding]) =
    (this, Http().newServerAt(interface, port).bind(route(())))
}
