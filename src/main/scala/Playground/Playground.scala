package Playground

import akka.actor.ActorSystem

object Playground extends App { 
  val actorSystem = ActorSystem("HiAkka")
  println(actorSystem.name)
}
