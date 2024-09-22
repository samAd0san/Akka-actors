package part4streams.udemy

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.{ExecutionContextExecutor, Future}

// 1. lecture 1 of Akka Streams
object FirstPrinciples extends App {
  implicit val system: ActorSystem = ActorSystem("FirstPrinciples")
  implicit val materializer: ExecutionContextExecutor = system.dispatcher // execution context
  //  implicit val materializer  = ActorMaterializer() // This is depreciated in Akka, but will still work

  // Source
  val source = Source(1 to 10)
  // Sink
  val sink = Sink.foreach[Int](println)
  // graph represents the stream's connections between its components
  val graph = source.to(sink)
  //  graph.run() // 1 2 3 4 5 6 7 8 9 10

  // flows transform element
  val flow = Flow[Int].map(x => x + 1)
  val sourceWithFlow = source.via(flow)
  val flowWithSink = flow.to(sink)

  // source.via(flow).to(sink).run() // 2 3 4 5 6 7 8 9 10 11
  //  sourceWithFlow.to(sink).run() // 2 3 4 5 6 7 8 9 10 11
  //  source.to(flowWithSink).run() // 2 3 4 5 6 7 8 9 10 11

  // various kinds of Sources
  val finiteSource = Source.single(1)
  val anotherFiniteSource = Source(List(1, 2, 3))
  val emptySource = Source.empty[Int]
  val infiniteSource = Source(Stream.from(1))
  anotherFiniteSource.runForeach(i => println(i)) // 1 2 3 (ref. docs)


  val futureSource = Source.fromFuture(Future(42))

  // sinks
  val theMostBoringSink = Sink.ignore
  val foreachSink = Sink.foreach[String](println)
  val headSink = Sink.head[Int] // retrieves head and then closes the stream
  val foldSink = Sink.fold[Int, Int](0)((a, b) => a + b)

  // flows - usually mapped to collection operators
  val mapFlow = Flow[Int].map(x => 2 * x)
  val takeFlow = Flow[Int].take(5)
  // drop, filter
  // NOT have flatMap

  /**
   * Exercise: create a stream that takes the names of persons, then you will keep the first 2 names with length > 5 characters.
   *
   */
  // Source
  val sourceNames = Source(List("Haroon", "Musa", "Zain", "WaraqAlJundul", "HabatRiaH"))
  val takeOnlyTwoFlow = Flow[String].filter(str => str.length > 5).take(2)
  val getName = Sink.foreach[String](println)

  sourceNames.via(takeOnlyTwoFlow).to(getName).run() // Haroon WaraqAlJundul
}