package part2actors

import akka.actor.{Actor, ActorSystem, Props}

object ActorsIntro extends App {
// This code demonstrates the basic setup and usage of actors in Akka

  // 1. Setup Actor System
  // An ActorSystem is a container that manages the lifecycle of actors
  val actorSystem = ActorSystem("firstActorSystem")
  println(actorSystem.name) // firstActorSystem

  // 2. Create actors
  // Word count actor
  class WordCountActor extends Actor {
    // Internal Data
    // It serves as a simple example of how actors can maintain and
    // update internal state based on the messages they receive.
    var totalWords = 0

    // Behavior
    def receive: Receive = {
      case message: String =>
        println(s"I have received a message: $message")
        totalWords += message.split(" ").length

      case msg => println(s"I cannot understand ${msg.toString}")
    }
  }

  // 3. Instantiate our actor
  var wordCounter = actorSystem.actorOf(Props[WordCountActor], "wordCounter")

  // 4. Communicate
  wordCounter ! "I am learning Akka and it's pretty cool!"
  // asynchronously
}
