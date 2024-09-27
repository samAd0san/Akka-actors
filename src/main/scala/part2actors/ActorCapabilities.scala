package part2actors
// lecture 2
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
  val system = ActorSystem("actorCapabilitiesDemo") // the name in quotes is unique
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

  /*
  Exercise
  1. a Counter actor
    - Increment
    - Decrement
    - Print
   */
  object Counter {
    case object Increment
    case object Decrement
    case object Print
  }

  class Counter extends Actor {
    import Counter._
    var count = 0

    override def receive: Receive = {
      case Increment => count += 1
      case Decrement => count -= 1
      case Print => println(s"[Counter] My Count is $count")
    }
  }

  val counter = system.actorOf(Props[Counter],"myCounter")
  import Counter._
  (1 to 5).foreach(_ => counter ! Increment) // Incrementing 5 times
  (1 to 3).foreach(_ => counter ! Decrement) // Decrementing 3 times
  counter ! Print // 2

  /*
  * 2. a Bank account as an actor
      receives
      - Deposit an amount
      - Withdraw an amount
      - Statement
      replies with
      - Success
      - Failure
      * For the Person interact with some other kind of actor
   */

  object BankAccount {
    case class Deposit(amount: Int)
    case class Withdraw(amount: Int)
    case object Statement

    case class TransactionSuccess(message: String)
    case class TransactionFailure(reason: String)
  }

  // Creating an actor for Bank Account
  class BankAccount extends Actor {

    import BankAccount._

    var funds = 0

    override def receive: Receive = {
      case Deposit(amount) =>
        // sender() is predefined in Akka and refers to the actor that sent the current message.
        if (amount < 0) sender() ! TransactionFailure("Enter valid amount")
        else {
          funds += amount
          sender() ! TransactionSuccess(s"Amount Deposited $amount")
        }

      case Withdraw(amount) =>
        if (amount < 0) sender() ! TransactionFailure("Enter Valid amount")
        else if (amount > funds) sender() ! TransactionFailure("Insufficient Balance")
        else {
          funds -= amount
          sender() ! TransactionSuccess(s"Amount withdrew $amount")
        }

      case Statement => sender() ! s"Your Balance is $funds"
    }
  }
  // Creating an actor for user who performs txns
  object Person {
    case class LiveTheLife(account: ActorRef)
  }

  class Person extends Actor {
    import Person._
    import BankAccount._

    override def receive: Receive = {
      case LiveTheLife(account) =>
        // Performing txns
        account ! Deposit(10_000) // TransactionSuccess(Amount Deposited 10000)
        account ! Withdraw(90_000) // TransactionFailure(Insufficient Balance)
        account ! Withdraw(500) // TransactionSuccess(Amount withdrew 500)
        account ! Statement // Your Balance is 9500
        account ! Withdraw(1490) // TransactionSuccess(Amount withdrew 1490)
        account ! Statement // Your Balance is 8010

      case message => println(message.toString)
    }
  }
  val account = system.actorOf(Props[BankAccount],"bankAccount")
  val person = system.actorOf(Props[Person],"billionaire")

  person ! Person.LiveTheLife(account)
}
