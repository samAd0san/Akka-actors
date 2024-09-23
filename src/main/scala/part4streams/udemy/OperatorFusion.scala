package part4streams.udemy

import akka.actor.{Actor, ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.ExecutionContextExecutor

// Lecture 3

/**
 * Operator Fusion in Akka Streams is where multiple stream processing stages (like map, filter, etc.) are executed within the same actor or thread, reducing the overhead of passing data between different stages.
 * Instead of treating each stage as a separate processing entity, operator fusion allows the stages to be "fused" together, so the data flows through them more efficiently, minimizing context switching, scheduling, and message-passing overhead.
 *
 * But A Single actor takes much time to execute if there is a time consuming computation, so we distribute
 * the phases to different actors using 'async' keyword, (refer the last lines of code)
 */
object OperatorFusion extends App {
  implicit val system: ActorSystem = ActorSystem("OperatorFusionSystem")
  implicit val materializer: ExecutionContextExecutor = system.dispatcher

  val simpleSource = Source(1 to 1000)
  val simpleFlow = Flow[Int].map(_ + 1)
  val simpleFlow2 = Flow[Int].map(_ * 10)
  val simpleSink = Sink.foreach[Int](println)

  // this runs on the SAME ACTOR --> operator/component FUSION
//  simpleSource.via(simpleFlow).via(simpleFlow2).to(simpleSink).run()

  // "equivalent" behavior
  class SimpleActor extends Actor {
    override def receive: Receive = {
      case x: Int =>
        // flow operations
        val x2 = x + 1
        val y = x2 * 10
        // sink operation
        println(y)
    }
  }

  val simpleActor = system.actorOf(Props[SimpleActor])
//  (1 to 1000).foreach(simpleActor ! _)

  val complexFlow = Flow[Int].map {
    x =>
      Thread.sleep(1000) // 1 sec
      x + 1
  }

  val complexFlow2 = Flow[Int].map {
    x =>
      Thread.sleep(1000)
      x * 10
  }
  // This takes more time since it is running on one actor
  simpleSource.via(complexFlow).via(complexFlow2).to(simpleSink).run() // This will take time since it is handled by a single operator

  // This takes comparatively less time because all the different phases are being executed on different actors
  // async boundary - dividing the phases b/w different actors using async keyword
  simpleSource.via(complexFlow).async // runs on one actor
    .via(complexFlow2).async // runs on another actor
    .to(simpleSink) // runs on third actor
//    .run()

}