package docs.ActorsIntro

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.LoggerOps
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}

// Acts as a Receptionist (Responds when it receives a message from HelloWorldBot(customer))
object HelloWorld {
  final case class Greet(whom: String, replyTo: ActorRef[Greeted])

  final case class Greeted(whom: String, from: ActorRef[Greet])

  def apply(): Behavior[Greet] = Behaviors.receive { (context, message) =>
    context.log.info("Hi {}!", message.whom)
    message.replyTo ! Greeted(message.whom, context.self)
    Behaviors.same
  }
}

// Acts as a customer (It sends the message to the HelloWorld(Receptionist) expecting a response back from it.
object HelloWorldBot {

  def apply(max: Int): Behavior[HelloWorld.Greeted] = {
    bot(0, max)
  }


  private def bot(greetingCounter: Int, max: Int): Behavior[HelloWorld.Greeted] =
    Behaviors.receive { (context, message) =>
      val n = greetingCounter + 1
      context.log.info2("Greeting {} for {}", n, message.whom)
      if (n == max) {
        Behaviors.stopped
      } else {
        message.from ! HelloWorld.Greet(message.whom, context.self)
        bot(n, max)
      }
    }
}

// It acts as the initiator, sending the initial SayHello message to start the communication.
object HelloWorldMain {

  final case class SayHello(name: String)

  def apply(): Behavior[SayHello] =
    Behaviors.setup { context =>
      val greeter = context.spawn(HelloWorld(), "greeter")

      Behaviors.receiveMessage { message =>
        val replyTo = context.spawn(HelloWorldBot(max = 3), message.name)
        greeter ! HelloWorld.Greet(message.name, replyTo)
        Behaviors.same
      }
    }

  def main(args: Array[String]): Unit = {
    val system: ActorSystem[HelloWorldMain.SayHello] =
      ActorSystem(HelloWorldMain(), "hello")

    system ! HelloWorldMain.SayHello("World")
    system ! HelloWorldMain.SayHello("Akka")
  }
}

/*
HelloWorldMain acts as the initiator, sending the initial SayHello message to start the communication.
HelloWorld and HelloWorldBot then take over the ongoing communication, with the HelloWorldBot sending Greet messages and the HelloWorld actor responding with Greeted messages.
This pattern is common in Akka applications, where a higher-level actor can coordinate the interactions between other actors.

ez words
Just to initialize the communication the HelloWorldMain is used (SayHello) and once the communication is established HelloWorld and HelloWorldBot both facilitates the communication
 */