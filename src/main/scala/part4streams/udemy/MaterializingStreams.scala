package part4streams.udemy

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}

import scala.util.{Failure, Success}

/**
 * 1. The Stream is static until we trigger the run method and that is called materializing
 * 2. Akka Stream only produces single materialized value when its trigger, its our job to choose
 * which materialized value we want to choose.
 */

object MaterializingStreams extends App {
  implicit val system = ActorSystem("MaterializingStreams")
  implicit val materializer = system.dispatcher

    Source(1 to 10).to(Sink.foreach[Int](println)).run() // Simple runnable graph

  val source = Source(1 to 10)
  val sink = Sink.reduce[Int]((a, b) => a + b) // a is accumulator, b is current element.

  val sumFuture = source.runWith(sink)
    sumFuture.onComplete {
      case Success(value) => println(s"The sum of all the elements is $value")
      case Failure(exception) => println(s"Error: $exception")
    }

  // Choosing Materialized Value
  val simpleSource = Source(1 to 10)
  val simpleFlow = Flow[Int].map(i => i + 1)
  val simpleSink = Sink.foreach[Int](println)

  val graph = simpleSource.viaMat(simpleFlow)(Keep.right).toMat(simpleSink)(Keep.right)
      graph.run().onComplete{
        case Success(value) => println("Stream processing finished")
        case Failure(exception) => println(s"[Error]: $exception")
      }
  // sugar syntax
  Source(1 to 10).runWith(Sink.reduce[Int](_ + _))
      .onComplete {
        case Success(value) => println(s"$value")
        case Failure(exception) => println(s"$exception")
      }

  Source(1 to 10).runReduce[Int](_ + _)
        .onComplete{
        case Success(value) => println(s"$value")
        case Failure(exception) => println(s"$exception")
      }

  // backwards
    Sink.foreach[Int](println).runWith(Source.single(10)) // 10
    Flow[Int].map(x => x * 2).runWith(simpleSource, simpleSink) // 2 4 6 8 10 12 14 16 18 20

  /**
   * - return the last element out of a source (use Sink.last)
   * - compute the total word count out of a stream of sentences
   *   - map, fold, reduce
   */

  // return the last element out of a source (use Sink.last)
  Source(List(1, 2, 3, 4, 5)).toMat(Sink.last)(Keep.right).run()
    .onComplete {
      case Success(value) => println(s"The last element is: $value")
      case Failure(exception) => println(s"Failed with: $exception")
    }

  /** OR */
  Source(List(1, 2, 3, 4, 5)).runWith(Sink.last)
    .onComplete {
      case Success(value) => println(s"The last element is: $value")
      case Failure(exception) => println(s"Failed with: $exception")
    }

  /**
   * - compute the total word count out of a stream of sentences
   * *   - map, fold, reduce
   */

  // Source
  val sentenceSource = Source(List("samad", "sohaib", "rabe3", "abu sulaym"))
  // Using Sink
  val wordsCountSink = Sink.fold[Int, String](0)((currentWords, newSentences) => currentWords + newSentences.split(" ").length)
  val way1 = sentenceSource.toMat(wordsCountSink)(Keep.right).run()
  val way2 = sentenceSource.runWith(wordsCountSink)
  val way3 = sentenceSource.runFold(0)((currentWords, newSentences) => currentWords + newSentences.split(" ").length)

  // Also by using Flow we can print the value
  val wordCountFlow = Flow[String].fold[Int](0)((currentWords, currentSentence) => currentWords + currentSentence.split(" ").length)
  val way4 = sentenceSource.via(wordCountFlow).toMat(Sink.head)(Keep.right).run()
  val way5 = sentenceSource.viaMat(wordCountFlow)(Keep.left).toMat(Sink.head)(Keep.right).run()
  val way6 = sentenceSource.via(wordCountFlow).runWith(Sink.head)
  val way7 = wordCountFlow.runWith(sentenceSource, Sink.head)._2

}
/**
 * Note: Each Source, Flow, and Sink may have their own materialized value, depending on how they are defined. The materialized value can vary, such as a Future, NotUsed, or other values that represent the outcome of running the stream.
 * Explanation:
 * Source: Can have a materialized value, such as NotUsed, a Future, or other types depending on how it's created.
 * Flow: Usually does not produce a materialized value unless explicitly designed to do so, but when combined with viaMat, it can modify the materialized value.
 * Sink: Often produces materialized values, such as a Future, that represent the result of the computation (e.g., Sink.reduce produces a Future).
 */