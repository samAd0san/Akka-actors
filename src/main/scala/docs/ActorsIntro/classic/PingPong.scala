package docs.ActorsIntro.classic

import akka.actor.{Actor, ActorRef, ActorSystem, PoisonPill, Props}

/** Terms
 * PoisonPill -  This message signals an actor to terminate itself.
 * Ping - Message sent by the Pinger actor to initiate communication.
 * Pong - Reply message sent by the Ponger actor in response to Ping.
 * */

object PingPong {
  case object Ping // Message sent by the Pinger actor to initiate communication.
  case object Pong // Reply message sent by the Ponger actor in response to Ping.

  class Pinger extends Actor {
    var countDown = 5

    override def receive: Receive = {
      case Pong =>
        println(s"${self.path.name}: message received $countDown")

        if(countDown > 0) {
          countDown -= 1
          sender() ! Ping
        }else {
          sender() ! PoisonPill
        }
    }
  }

  class Ponger(pinger: ActorRef) extends Actor {
    override def receive: Receive = {
      case Ping =>
        println(s"${self.path.name}: message sent")

        pinger ! Pong
    }
  }

  def main(args: Array[String]): Unit = {
    val system = ActorSystem("pingpong")

    val pinger = system.actorOf(Props[Pinger], "pinger")
    val ponger = system.actorOf(Props(classOf[Ponger],pinger),"ponger")

    ponger ! Ping
  }
}