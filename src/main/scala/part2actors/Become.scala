package part2actors

import akka.actor.{Actor, ActorSystem, Props}
/** Example to demonstrate become() method */
object Become {

  class MoodActor extends Actor {

    override def receive: Receive = happy

    def happy: Receive = {
      case "how are you" => println("I am happy")
      case "mood change" => context.become(sad) // Switch to Sad behaviour
    }

    def sad: Receive = {
      case "how are you" => println("I am Sad")
      case "mood change" => context.become(happy) // Switch to happy behaviour
    }
  }

  def main(args: Array[String]): Unit = {
    // Creating an ActorSystem and a moodActor
    val system = ActorSystem("moodSystem")
    val moodActor = system.actorOf(Props[MoodActor], "moodActor")

    // Interacting with the actor
    moodActor ! "how are you" // happy
    moodActor ! "mood change"

    moodActor ! "how are you" // sad
    moodActor ! "mood change"
    moodActor ! "how are you" // happy
  }
}

/**
 * Explanation:
 * The actor starts in a "happy" state.
 * When it receives the message "change mood", it switches to the "sad" behavior using context.become(sad).
 * When it's in the "sad" state, another "change mood" switches it back to "happy" using context.become(happy).
 * This allows the actor to dynamically change how it responds to the same message based on its current state.
 */

/**
 * WHAT IS BECOME AND UNBECOME METHOD??
 * In Akka, become and unbecome allow an actor to change its behavior dynamically during runtime.
 *
 * become: Changes the actor's behavior to a new set of message handlers. This means the actor starts handling messages differently.
 * unbecome: Reverts the actor's behavior back to what it was before the last become call.
 * This is useful when the actor needs to respond differently based on its state. You can switch between different behaviors without creating new actors or stopping the current one.
 */