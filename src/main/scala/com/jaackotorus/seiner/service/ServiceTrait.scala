package com.jaackotorus.seiner
package service

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route

import scala.concurrent.{ExecutionContextExecutor, Future}

trait ServiceTrait[T <: Any, U <: Service[T]] {
  def apply(
      interface: String,
      port: Int,
      routeGenerator: T => Route
  ): U
}

abstract class Service[T <: Any](
    val interface: String,
    val port: Int,
    val route: T => Route
) {
  implicit val system: ActorSystem
  implicit val context: ExecutionContextExecutor
  def start: (Service[T], Future[Http.ServerBinding])
}
