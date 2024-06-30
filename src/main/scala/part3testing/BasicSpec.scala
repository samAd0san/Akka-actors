package part3testing

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

import scala.util.Random

class BasicSpec extends TestKit(ActorSystem("BasicSpec"))
  with ImplicitSender
  with AnyWordSpecLike
  with BeforeAndAfterAll {

  // setup
  override def afterAll(): Unit = {
    // 'system' refers to the ActorSystem instance used for creating and managing actors in your tests.
    TestKit.shutdownActorSystem(system)
  }

  import BasicSpec._

  "a simple actor" should { // 'should' describes the test scenario,
    "send back the same message" in { // 'in' defines the test logic or the expected behavior for that scenario.
      val echoActor = system.actorOf(Props[SimpleActor])
      echoActor ! "Hello, sup this is a test message"
    }
  }

  " a black hole" should {
    "send back some message" in {
      val blackhole = system.actorOf(Props[Blackhole])
      blackhole ! "Another test message"
    }
  }

  // message assertion - verifying that messages are sent, received, or that certain conditions are met during the test.
  "A lab test actor" should {
    // defining an actor
    val labTestActor = system.actorOf(Props[LabTestActor])

    // Checking if the string is successfully turned to uppercase
    "turn a string to uppercase" in {
      labTestActor ! "I Love Akka"
      val reply = expectMsgType[String]

      assert(reply == "I LOVE AKKA")
    }

    "reply to a greeting" in {
      labTestActor ! "greeting"
      expectMsgAnyOf("hello","hi") // anyOf - either hello/hi
    }

    "reply with your fav tech" in {
      labTestActor ! "favoriteTech"
      expectMsgAllOf("Scala","Akka") // allOf - both should be present i.e Scala & Akka
    }


  }
}

object BasicSpec {

  class SimpleActor extends Actor {
    override def receive: Receive = {
      case message => sender() ! message
    }
  }

  class Blackhole extends Actor {
    override def receive: Receive = Actor.emptyBehavior
  }

  class LabTestActor extends Actor {

    val random = new Random()
    override def receive: Receive = {
      case "greeting" =>
        if (random.nextBoolean()) sender() ! "hi" else "hello" // if random is true it'll print hi else hello
      case "favoriteTech" =>
        sender() ! "Scala"
        sender() ! "Akka"
      case message: String =>
        sender() ! message.toUpperCase()
    }
  }
}