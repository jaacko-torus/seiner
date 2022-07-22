package com.jaackotorus.seiner
package actor

import akka.actor._
import com.github.nscala_time.time.Imports.DateTime

import scala.collection.mutable

object UserActor {
  abstract class Event(datetime: DateTime) {
    lazy val timestamp: String = datetime.toString("hh:mma").toLowerCase
  }

  case class User(name: String)

  case class `User&Message`(user: String, message: String)

  object Event {
    case class None()(implicit datetime: DateTime) extends Event(datetime)
    case class UserJoined(username: String, actor: ActorRef)(implicit
        datetime: DateTime
    ) extends Event(datetime)
    case class UserLeft(username: String)(implicit datetime: DateTime) extends Event(datetime)
    case class UserSentMessage(username: String, messages: Vector[String])(implicit
        datetime: DateTime
    ) extends Event(datetime)
  }
}

class UserActor extends Actor {
  import UserActor._

  implicit val users: mutable.Map[String, (User, ActorRef)] =
    mutable.LinkedHashMap[String, (User, ActorRef)]()

  override def receive: Receive = {
    case event: Event.UserJoined =>
      users += event.username -> (User(event.username), event.actor)
      broadcast(event)
    case event: Event.UserLeft =>
      users -= event.username
      broadcast(event)
    case event: Event.UserSentMessage => {
      println("message received")
      println(event)

      broadcast(event)
    }
  }

  def broadcast(data: Event): Unit = {
    users.foreach { case (_, (_, actor)) => actor ! data }
  }
}
