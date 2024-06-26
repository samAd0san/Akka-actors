package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorCapabilities extends App {

  // Creating an actor
  class SimpleActor extends Actor {
    override def receive: Receive = { // This is a Partial function
      case message: String => println(s"[$self] I have received a message: $message")
      case number: Int => println(s"[simple actor] I have received a number: $number")
      case SpecialMessage(contents) => println(s"[simple actor] I have received special message: $contents")

      // fetching info about actors (self)
      case SendMessageToYourSelf(content) =>
        self ! content // we use $self to see the info about the actor

      // Reply to an actor
      case SayHiTo(ref) => ref ! "Hi!" // ref is reference to an actor instance // alice is being passed as the sender

      // the actor forwards a message appended with "s" to the actor referenced by ref
      case WirelessPhoneMessage(content, ref) => ref forward(content + "s")
      // The forward method in Akka allows an actor to pass along a received message to another actor while maintaining the original sender information intact
    }
  }

  // creating actor system
  val system = ActorSystem("actorCapabilitiesDemo") // the name is quotes is unique
  val simpleActor = system.actorOf(Props[SimpleActor],"simpleActor")

  // 1. We can send messages of any type to actor
  // a) messages must be IMMUTABLE
  // b) messages must be SERIALIZABLE

  simpleActor ! "Hello, actor" // [simple actor] I have received a message: Hello, actor
  simpleActor ! 42 // [simple actor] I have received a number: 42

  case class SpecialMessage(contents: String)
  simpleActor ! SpecialMessage("Some Special Message!!") // [simple actor] I have received special message: Some Special Message!!

  // 2. actors have information about their context and about themselves
  // context.self === `this` in OOP

  case class SendMessageToYourSelf(content: String)
  simpleActor ! "I am an actor" // [Actor[akka://actorCapabilitiesDemo/user/simpleActor#-361106851]] I have received a message: I am an actor

  // 3. actors can reply to messages
  // Create two actor instances: alice and bob.
  val alice = system.actorOf(Props[SimpleActor],"alice")
  val bob = system.actorOf(Props[SimpleActor],"bob")

  case class SayHiTo(ref: ActorRef) // ActorRef is a reference to an actor that allows you to send messages to it.
  // Sends a message to the alice actor, instructing it to send a "Hi!" message to the bob actor.
  alice ! SayHiTo(bob) // [Actor[akka://actorCapabilitiesDemo/user/bob#-1335224202]] I have received a message: Hi!

  // 4 - Dead letters
  alice ! "Hi!" // [Actor[akka://actorCapabilitiesDemo/user/alice#-91800820]] I have received a message: Hi!
  // 'alice' sends "Hello, there!" to the sender of "Hi!" message
  // No sender specified, so the reply goes to the dead letters mailbox

  // 5 - forwarding messages
  // D -> A -> B
  // forwarding = sending a message with the ORIGINAL sender
  case class WirelessPhoneMessage(content: String, ref: ActorRef)

  alice ! WirelessPhoneMessage("Hi", bob) // [Actor[akka://actorCapabilitiesDemo/user/bob#-1318655697]] I have received a message: His
  // bob receives "His" forwarded by alice with the original sender preserved.
}
