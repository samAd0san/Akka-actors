package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.ChangingActorBehaviour.Counter.{Decrement, Increment, Print}
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

  /*
  Exercise 1 - recreate the Counter Actor with context.become and NO MUTABLE STATE
   */
  object Counter {
    case object Increment
    case object Decrement
    case object Print
  }

  class Counter extends Actor {

    override def receive: Receive = Counter(0)

    def Counter(count: Int): Receive = {
      case Increment =>
        println("++")
        context.become(Counter(count + 1))
      case Decrement =>
        println("--")
        context.become(Counter(if (count > 0) count - 1 else 0))
      case Print =>
        println(s"[Counter] $count")
    }
  }
  var counter = system.actorOf(Props[Counter])
  (1 to 10).foreach(_ => counter ! Increment) // adding 5 times
  (1 to 3).foreach(_ => counter ! Decrement) // subtracting 3 times
  counter ! Print // [Counter] 7

  /*
  Exercise 2 - Voting System
  */

  case class Vote(candidate: String)
  case object VoteStatusRequest
  case class VoteStatusReply(candidate: Option[String])

  // Creating an actor for Citizen
  class Citizen extends Actor {

    // Initial behavior of the Citizen actor ( i.e before voting)
    override def receive: Receive = {
      // When the actor receives a Vote message, it changes its behavior
      case Vote(c) =>
      // context.become(voted(c)) changes the behavior of the actor to the 'voted' state with the specified candidate
        context.become(voted(c))

      // When the actor receives a VoteStatusRequest message, it responds with VoteStatusReply(None)
      case VoteStatusRequest =>
        // Since the actor has not yet voted it'll reply with null
        sender() ! VoteStatusReply(None)
    }

    // Behavior after the citizen has voted for a candidate
    def voted(candidate: String): Receive = {
      // When the actor receives a VoteStatusRequest message in the 'voted' state,
      // it responds with VoteStatusReply(Some(candidate))
      case VoteStatusRequest =>
      // It replies with Some(candidate), indicating the candidate they voted for
        sender() ! VoteStatusReply(Some(candidate))
    }
  }

  // This message class is used to start the aggregation process, containing a set of citizen actor references.
  case class AggregateVotes(citizens: Set[ActorRef])


  class VoteAggregator extends Actor {
    // Initial behavior of the VoteAggregator actor
    override def receive: Receive = awaitingCommand

    // The 'awaitingCommand' behavior, waiting for the command to start aggregation
    def awaitingCommand: Receive = {
      case AggregateVotes(citizens) =>
      // Send a VoteStatusRequest to each citizen
        citizens.foreach(citizenRef => citizenRef ! VoteStatusRequest)
      // Change the behavior to 'awaitingStatuses' with the set of citizens and an empty currentStats map
        context.become(awaitingStatuses(citizens,Map()))
    }

    // The 'awaitingStatuses' behavior, waiting for responses from the citizens
    def awaitingStatuses(stillWaiting: Set[ActorRef], currentStats: Map[String, Int]): Receive = {
    // When a citizen replies with None, indicating they haven't voted yet
    case VoteStatusReply(None) =>
    // Request the vote status again (may cause an infinite loop if a citizen never votes)
      sender() ! VoteStatusRequest

    // When a citizen replies with Some(candidate), indicating they have voted
    case VoteStatusReply(Some(candidate)) =>
      // Remove the citizens from the waiting list
      val newStillWaiting = stillWaiting - sender()
      // Get the current vote count for the candidate, defaulting to 0 if the candidate is not yet in the map
      val currentVoteCandidate = currentStats.getOrElse(candidate, 0)
      // Update the stats with the new vote count for the candidate
      val newStats = currentStats + (candidate -> (currentVoteCandidate + 1))

      // If no citizens are left to respond
      if (newStillWaiting.isEmpty) {
        println(s"[aggregator] poll stats: $newStats")
      } else {
        // If there are still citizens to respond, update the behavior with the new stillWaiting set and newStats map
        context.become(awaitingStatuses(newStillWaiting,newStats))
      }
    }
  }
  // Create citizen actors
  val sobhi = system.actorOf(Props[Citizen], "sobhi")
  val samad = system.actorOf(Props[Citizen], "samad")
  val chandler = system.actorOf(Props[Citizen], "chandler")
  val daniel = system.actorOf(Props[Citizen], "daniel")

  // Citizens voting for the candidates
  sobhi ! Vote("Martin")
  samad ! Vote("Martin")
  chandler ! Vote("Jonas")
  daniel ! Vote("Roland")

  // Create vote aggregator actor
  val voteAggregator = system.actorOf(Props[VoteAggregator], "voteAggregator")

  // Aggregate votes from all citizens
  voteAggregator ! AggregateVotes(Set(sobhi, samad, chandler, daniel)) // [aggregator] poll stats: Map(Martin -> 2, Jonas -> 1, Roland -> 1)
}
