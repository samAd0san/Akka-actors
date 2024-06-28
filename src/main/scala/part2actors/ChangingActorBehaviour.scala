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

  // Defining the methods that will be used by the actors to communicate
  case class Vote(candidate: String) // Vote is used to send a candidate's name to the Citizen actor,
  // indicating that the citizen is casting a vote for that candidate.

  case object VoteStatusRequest // It is used to request the voting status of a citizen from the Citizen actor.

  /* Option[String] is used as an argument to handle the possibility that a citizen may
  not have voted yet, representing a vote with Some(candidate) or no vote with None. */
  case class VoteStatusReply(candidate: Option[String]) //  is used to respond with the voting status of a citizen,
  // indicating which candidate (if any) they voted for.

  /* Actor 1 */
  // This actor will handle voting and responding to vote status requests.
  class Citizen extends Actor {
    // This variable can store only one value: either None or Some(String)
    var candidate: Option[String] = None // Initially, no vote

    override def receive: Receive = {
      // Store the candidate when voting
      case Vote(c) => candidate = Some(c) // e.g Some("Martin")
      // Reply with the voting status
      case VoteStatusRequest => sender() ! VoteStatusReply(candidate) //  when a VoteStatusRequest is received, the Citizen actor
      // replies to the sender with the current voting status (either None or Some(candidate)).
    }
  }


  // It is used to initiate the process of collecting votes from a set of Citizen actors.
  case class AggregateVotes(citizens: Set[ActorRef]) // We are taking Set to ensure each citizen actor is
  // unique and to prevent duplicate entries.

  /* Actor 2 */
  // This actor will aggregate votes from a set of citizens and print the results.
  class VoteAggregator extends Actor {
  // To track citizens who haven't replied.
  var stillWaiting: Set[ActorRef] = Set()

  // To Track vote counts
  var currentStats: Map[String, Int] = Map()

  override def receive: Receive = {
    case AggregateVotes(citizens) =>
      stillWaiting = citizens // Set the citizens who need to apply
      citizens.foreach(citizenRef => citizenRef ! VoteStatusRequest) // Ask each citizen for their vote

    case VoteStatusReply(Some(candidate)) =>
      val newStillWaiting = stillWaiting - sender() // // Remove the citizen from the waiting list
      // This returns the count of the candidate
      val currentVotesOfCandidates = currentStats.getOrElse(candidate, 0) // Get the current vote count for the candidate

      currentStats = currentStats + (candidate -> (currentVotesOfCandidates + 1)) // // Update the vote count

      if(newStillWaiting.isEmpty) {
        println(s"[aggregator] poll stats: $currentStats") // Print the final results if all have replied
      }else{
        stillWaiting = newStillWaiting // // Update the waiting list
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
