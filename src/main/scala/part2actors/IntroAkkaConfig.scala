package part2actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object IntroAkkaConfig extends App {
  val config = ConfigFactory.load() // Loads application.conf by default
  val system = ActorSystem("MySystem", config)
  val actor = system.actorOf(Props[AkkaConfigActor], "myAkkaConfigActor")

  actor ! "Hello, Akka"
}

class AkkaConfigActor extends Actor with ActorLogging {
  override def receive: Receive = {
    case message =>
      log.info(s"Received Message: $message")
  }
}
