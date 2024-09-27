package part4streams.udemy
/** Lecture 4 */
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Sink, Source}
import scala.concurrent.ExecutionContextExecutor
/**
 Backpressure controls the flow of data, ensuring that fast producers do not overwhelm slower consumers by
 allowing consumers to signal their capacity to handle data, thus preventing buffer overflow or system
 crashes.
 */
object BackPressureBasics extends App {
  implicit val system: ActorSystem = ActorSystem("BackPressureSystem")
  implicit val materializer: ExecutionContextExecutor = system.dispatcher

  val fastSource = Source(1 to 1000)
  val slowSink = Sink.foreach[Int]{ x =>
    // stimulate a long processing
    Thread.sleep(1000)
    println(s"Sink: ${x}")
  }

//  fastSource.to(slowSink).run() // This is not BackPressure because all the phases are being handled by one actor only, It is Operator Fusion.
//  fastSource.async.to(slowSink).run() // This is BackPressure because Source and Sink is handled by different Actor

  val simpleFlow = Flow[Int].map { x =>
    println(s"Incoming $x")
    x + 1
  }

  fastSource.via(simpleFlow).async
    .to(slowSink).async
//    .run()

  val bufferedFlow = simpleFlow.buffer(10, overflowStrategy = OverflowStrategy.dropHead)

  fastSource.async
    .via(bufferedFlow).async
    .to(slowSink)
//    .run()

  /*
    1-16: nobody is backpressured
    17-26: flow will buffer, flow will start dropping at the next element
    26-1000: flow will always drop the oldest element
      => 991-1000 => 992 - 1001 => sink
   */

  /*
    overflow strategies:
    - drop head = oldest
    - drop tail = newest
    - drop new = exact element to be added = keeps the buffer
    - drop the entire buffer
    - backpressure signal
    - fail
   */

  // throttling
  import scala.concurrent.duration._
//  fastSource.throttle(10, 1.second).runWith(Sink.foreach(println))
}