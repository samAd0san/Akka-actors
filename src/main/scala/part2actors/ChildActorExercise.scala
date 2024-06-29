package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ChildActorExercise extends App {
  /*
  Workflow:
    1. The TestActor sends an "Initialize" message to the WordCounterMaster to create child workers.
    2. The TestActor then sends multiple text messages to the WordCounterMaster.
    3. The WordCounterMaster forwards each text message as a word count task to a WordCounterWorker.
    4. Each WordCounterWorker counts the words and sends the result back to the WordCounterMaster.
    5. The WordCounterMaster then sends the word count result back to the TestActor, which prints the result.
   */

  // Companion object for Master
  object WordCounterMaster {
    // Message to master than it should create new child actors
    case class Initialize(nChildren: Int)

    // To send the Worker a String so that it can send the count of it
    case class WordCountTask(id: Int, text: String)

    // On sending the task to the worker it'll send the count of the string to master
    case class WordCountReply(id: Int, count: Int)
  }

  // Actor 2. Master Actor
  class WordCounterMaster extends Actor {
    import WordCounterMaster._

    override def receive: Receive = {
      case Initialize(nChildren) =>
        println("[master] Initializing...")
        // Creating new Actors (child)
        val childrenRefs = for (i <- 1 to nChildren) yield context.actorOf(Props[WordCounterWorker], s"wcw_$i")

        // now we are changing the behavior of the actor i.e altering the messages it respond and how to respond to it.
        context.become(withChildren(childrenRefs, 0, 0, Map()))
    }

    def withChildren(childrenRefs: Seq[ActorRef], currentChildIndex: Int, currentTaskId: Int, requestMap: Map[Int, ActorRef]): Receive = {
      case text: String =>
        println(s"I have received the text $text -- Sending it to child $currentChildIndex")

        // assigning Id to a task and taking the children Actor from the Seq index wise i.e from 0,1,2...
        val task = WordCountTask(currentTaskId, text)
        val childRef = childrenRefs(currentChildIndex)
        childRef ! task // This mean the WordCountTask is being executed by the childRef

        // The modulo operator is used to ensure that the index cycles back to the start of the list once it reaches the end
        val nextChildIndex = (currentChildIndex + 1) % childrenRefs.length // If currentChildIndex is 4 and childrenRefs.length is 5, then nextChildIndex will be (4 + 1) % 5 = 0.
        val newTaskId = currentTaskId + 1 // next task

        val originalSender = sender()
        // e.g currentTaskId is 2 and originalSender is "actorRef3", so (currentTaskId -> originalSender) creates a tuple (2, "actorRef3"). The tuple is added to Map()
        val newRequestMap = requestMap + (currentTaskId -> originalSender) // eg Map(...) + (2 -> actorRef3) appending this key-value to Map

        // Calling the same method to assign the remaining task to other child actors (if available)
        context.become(withChildren(childrenRefs, nextChildIndex, newTaskId, newRequestMap))

      case WordCountReply(id, count) =>
        println(s"[master] I have received a reply for task from id $id with $count")
        // e.g If requestMap is Map(1 -> actorRef1, 2 -> actorRef2) and id is 1:
        val originalSender = requestMap(id) // e.g requestMap(1) it'll be childRef1
        originalSender ! count
        context.become(withChildren(childrenRefs, currentChildIndex,currentTaskId, requestMap - id)) // the task is removed from the requestMap, not the child.
    }
  }

  // Actor 1. Worker Actor
  class WordCounterWorker extends Actor {
    import WordCounterMaster._

    override def receive: Receive = {
      case WordCountTask(id, text) =>
        println(s"${self.path} I have received the task $id with text $text")
        // Counting the words and sending it back to the master
        sender() ! WordCountReply(id, text.split(" ").length)
    }
  }

  class TestActor extends Actor {
    import WordCounterMaster._

    override def receive: Receive = {
      case "go" =>
        val master = context.actorOf(Props[WordCounterMaster], "master")
        master ! Initialize(3) // Creating new 3 actors

        val text = List("I love Akka", "Scala is super dope", "yes", "me too")
        text.foreach(text => master ! text) // Sending each text present in the list iteratively to the master
      case count: Int =>
        println(s"[Test Actor] I received a reply: $count")
    }
  }

  val system = ActorSystem("roundRobinWordCountExercise")
  val testActor = system.actorOf(Props[TestActor], "testActor")
  testActor ! "go"

  /*
  OUTPUT

  [master] Initializing...
  I have received the text I love Akka -- Sending it to child 0
  I have received the text Scala is super dope -- Sending it to child 1
  akka://roundRobinWordCountExercise/user/testActor/master/wcw_1 I have received the task 0 with text I love Akka
  akka://roundRobinWordCountExercise/user/testActor/master/wcw_2 I have received the task 1 with text Scala is super dope
  I have received the text yes -- Sending it to child 2
  I have received the text me too -- Sending it to child 0
  akka://roundRobinWordCountExercise/user/testActor/master/wcw_3 I have received the task 2 with text yes
  akka://roundRobinWordCountExercise/user/testActor/master/wcw_1 I have received the task 3 with text me too
  [master] I have received a reply for task from id 0 with 3
  [master] I have received a reply for task from id 2 with 1
  [master] I have received a reply for task from id 1 with 4
  [master] I have received a reply for task from id 3 with 2
  [Test Actor] I received a reply: 3
  [Test Actor] I received a reply: 1
  [Test Actor] I received a reply: 4
  [Test Actor] I received a reply: 2
   */
}
