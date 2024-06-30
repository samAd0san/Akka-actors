package part2actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.event.Logging

object ActorLoggingDemo extends App {
  /*
  Actor logging in Akka is a mechanism for actors to log messages at various severity
  levels (DEBUG, INFO, WARNING, ERROR) to aid in debugging and monitoring actor behavior.
  */

  class SimpleActorWithExplicitLogger extends Actor {

    // Logging(): A utility in Akka that allows actors to log messages.
    // context.system: Refers to the ActorSystem to which the actor belongs, providing the logging system context.
    val logger = Logging(context.system, this) // 'this' refers to the current instance of the SimpleActorWithExplicitLogger class, enabling the logger to associate logs with this specific actor instance.
    override def receive: Receive = {
      case message =>
        logger.info(message.toString) // This will give log in the INFO level
    }
  }
  val system = ActorSystem("LoggingDemo")
  val actor = system.actorOf(Props[SimpleActorWithExplicitLogger])

  actor ! "Logging with a simple message"
  // O/P -> [INFO] [06/30/2024 11:11:01.387] [LoggingDemo-akka.actor.default-dispatcher-6] [akka://LoggingDemo/user/$a] Logging with a simple message

  // Using 'ActorLogging' trait in the actor
  class ActorLoggingDemo extends Actor with ActorLogging {
    override def receive: Receive = {
      case (a,b) => log.info("Two Things {} and {}",a,b)
      case message => log.info(message.toString)
    }
  }
  val simpleActor = system.actorOf(Props[ActorLoggingDemo])
  simpleActor ! "Logging a simple message by extending a trait" // [INFO] [06/30/2024 11:20:26.870] [LoggingDemo-akka.actor.default-dispatcher-6] [akka://LoggingDemo/user/$b] Logging a simple message by extending a trait
  simpleActor ! (4,5) // [INFO] [06/30/2024 11:20:26.870] [LoggingDemo-akka.actor.default-dispatcher-6] [akka://LoggingDemo/user/$b] Two Things 4 and 5
}
