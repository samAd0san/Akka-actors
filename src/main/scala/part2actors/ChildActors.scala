package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.actor.Actor.Receive
import akka.actor.TypedActor.self
import part2actors.ChildActors.Parent.{CreateChild, TellChild}

object ChildActors extends App {

  /*
  In this Code, We are Creating a new Actor (child) Inside an another Actor (parent)
  NOTE: We can only create one actor at a time (In this Code)
   */

  object Parent {
    case class CreateChild(name: String)
    case class TellChild(message: String)
  }

  class Parent extends Actor {

    override def receive: Receive = {
      // This case is for creating a new Actor
      case CreateChild(name) =>
        println(s"${self.path} Creating Child")
        // Creating a new Actor right here
        val ChildRef = context.actorOf(Props[Child], name)
        // Forwarding the message to the child, 'That is got created'
        context.become(withChild(ChildRef)) // Defining the reference to the newly created child
    }
        // This case is for sending a message to the child which is created
    def withChild(ChildRef: ActorRef): Receive = {
      case TellChild(message) =>
        // The forward method in Akka is used to send a message to another actor while preserving the original sender's information.
        ChildRef forward message
    }
  }

  class Child extends Actor {
    override def receive: Receive = {
      case message => println(s"${self.path} I got a message: $message")
    }
  }

  val system = ActorSystem("PrintChildDemo")
  val parent = system.actorOf(Props[Parent],"parent")

  parent ! CreateChild("child1")
  parent ! TellChild("Welcome kid")

  /**
   * Danger!
   *
   * NEVER PASS MUTABLE ACTOR STATE, OR THE `THIS` REFERENCE, TO CHILD ACTORS.
   *
   * NEVER IN YOUR LIFE.
   */
}
