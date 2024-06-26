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

    // Behavior -
    def receive: Receive = { // type Receive = PartialFunction[Any, Unit] i.e It takes Any type but only return Unit
      case message: String =>
        println(s"I have received a message: $message")
        totalWords += message.split(" ").length

      case msg => println(s"I cannot understand ${msg.toString}")
    }
  }

  // 3. Instantiate our actor
  var wordCounter = actorSystem.actorOf(Props[WordCountActor], "wordCounter")
  var anotherWordCounter = actorSystem.actorOf(Props[WordCountActor], "anotherWordCounter")

  // 4. Communicate
  // Using infix notation: wordCounter.!("I am learn...")
  // !: The tell method, used to send an asynchronous message.
  wordCounter ! "I am learning Akka and it's pretty cool!" // I have received a message: I am learning Akka and it's pretty cool!
  anotherWordCounter ! "A different message" // I have received a message: A different message
  // asynchronously

  // Another Example

  // Creating an actor
  class Person(name: String) extends Actor {
    override def receive: Receive = {
      case "hi" => println(s"Hi, my name is $name")
      case _ =>
    }
  }

  // Creating Props for instantiation of the actor (This method is used in the below code)
  object Person {
    def props(name: String) = Props(new Person(name))
  }

  // Instantiate our Person Actor
  val person = actorSystem.actorOf(Person.props("Bob Marley")) // using the object Person method here
  person ! "hi" // Hi, my name is Bob Marley
}
