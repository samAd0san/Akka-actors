package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.ChangingActorBehaviour.Mom.MomStart

/*
Changing actor behavior in Akka refers to the ability of an actor to dynamically change
the way it handles incoming messages by swapping its message handling function at runtime.
 */

object ChangingActorBehaviour extends App {
  /*
  This program consists of two actors 'child' and 'mom'
  If the child gets veg he is sad, and he is happy if he gets a chocolate
  If he is sad he will not play with mom, if he is happy he'll play
   */
  object FussyKid {
    case object KidAccept
    case object KidReject
    val HAPPY = "happy"
    val SAD = "sad"
  }

  class StatelessFussyKid extends Actor {
    import FussyKid._
    import Mom._

    override def receive:Receive = happyReceive

    def happyReceive : Receive = {
      // The false flag means the new behavior does not stack on the previous one; it replaces it.
      case Food(VEGETABLE) => context.become(sadReceive, false)
      case Food(CHOCOLATE) => // Remain happy, do nothing
      case Ask(_) => sender() ! KidAccept
    }

    def sadReceive : Receive = {
      case Food(VEGETABLE) => context.become(happyReceive, false)
      // context.unbecome(): Reverts the actor's behavior to the previous one in the stack.
      case Food(CHOCOLATE) => context.unbecome()
      case Ask(_) => sender() ! KidReject
    }
  }

  object Mom {
    case class MomStart(kidRef: ActorRef)
    case class Food(food: String)
    case class Ask(message: String)
    val VEGETABLE = "veggies"
    val CHOCOLATE = "chocolate"
  }

  class Mom extends Actor {
    import Mom._
    import FussyKid._

    override def receive: Receive = {
      case MomStart(kidRef) =>
        // test our interaction
        kidRef ! Food(VEGETABLE)
        kidRef ! Food(VEGETABLE)
        kidRef ! Food(CHOCOLATE)
        kidRef ! Food(CHOCOLATE) // -> yay, my kid is happy
        kidRef ! Ask("Do you wanna build a snow man?")

      case KidAccept => println("yay, my kid is happy")
      case KidReject => println("My kid is sad, but as he's healthy!")
    }
  }

  val system = ActorSystem("changingActorBehaviourDemo")
  val fussyKid = system.actorOf(Props[StatelessFussyKid])
  val mom = system.actorOf(Props[Mom])

  mom ! MomStart(fussyKid)
  /*
   mom receives MomStart
     kid receives Food(veg) -> kid will change the handler to sadReceive
     kid receives Ask(play?) -> kid replies with the sadReceive handler =>
   mom receives KidReject
  */
  /*
    Stateless fussyKid case
    Initial:         happyReceive
    Food(VEG):       sadReceive  // context.become(sadReceive, false)
    Food(VEG):       happyReceive // context.become(happyReceive, false)
    Food(CHOCOLATE): happyReceive // context.unbecome(), but no change as it's already happyReceive
    Food(CHOCOLATE): happyReceive // unchanged
   */
}
