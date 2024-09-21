package part2actors

import akka.actor.{Actor, ActorSystem, Props}

object Unbecome {
  /** Example to demonstrate unbecome() method */

  class MoodActor extends Actor {
    override def receive: Receive = happy // default it is happy

    def happy: Receive = {
      case "how are you" => println("I am happy :)")
      case "mood change" =>
        context.become(sad)
        println("changing the mood to Sad :/")
      case "revert" =>
        context.unbecome()
        println("Reverting back to the previous mood")
    }

    def sad: Receive = {
      case "how are you" => println("I am sad :(")
      case "revert" =>
        context.unbecome()
        println("Reverting back to the Happy mood!")
    }
  }

  def main(args: Array[String]): Unit = {
    val system = ActorSystem("moodSystem")
    val moodActor = system.actorOf(Props[MoodActor], "moodActor")

    moodActor ! "how are you" // I am happy :)
    moodActor ! "mood change" // changing the mood to Sad :/
    moodActor ! "how are you" // I am sad :(
    moodActor ! "revert" // Reverting back to the Happy mood
    moodActor ! "how are you" // I am happy :)

    moodActor ! "mood change" // changing the mood to Sad :/
    moodActor ! "how are you" // I am sad :(
    moodActor ! "revert" // Reverting back to the Happy mood!
    moodActor ! "how are you" // I am happy :)
  }
}

/**
 * Explanation:
 * The actor starts with the happy behavior.
 * When it receives "change mood", it switches to the sad behavior.
 * If it receives "revert", it uses unbecome to go back to the previous behavior (in this case, happy).
 * This shows how unbecome helps the actor return to its previous state before a behavior change.
 */

/**
 * WHAT IS BECOME AND UNBECOME METHOD??
 * In Akka, become and unbecome allow an actor to change its behavior dynamically during runtime.
 *
 * become: Changes the actor's behavior to a new set of message handlers. This means the actor starts handling messages differently.
 * unbecome: Reverts the actor's behavior back to what it was before the last become call.
 * This is useful when the actor needs to respond differently based on its state. You can switch between different behaviors without creating new actors or stopping the current one.
 */