package part4streams

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{FileIO, Framing, Sink, Source}
import akka.util.ByteString

import java.nio.file.Paths
import scala.reflect.io.Path

object AkkaStreamsExamples extends App {
  // Set up ActorSystem
  implicit val system: ActorSystem = ActorSystem("AkkaStreamExamples")

  // Task 1: Create a Source of integers, apply basic transformations (like map and filter), and print the results
  // Creating a Source of Integer from 1 to 10
  val source: Source[Int, NotUsed] = Source(1 to 10 )
  // Applying map function
  val mappedSource = source.map(_ * 2) // Multiply each element by 2
  // Applying filter function
  val filteredSource: Source[Int, NotUsed] = mappedSource.filter(_ % 3 == 0) // Filter out elements that are not divisible by 3
  // print the results
  filteredSource.runForeach(println) // 6 12 18

  // Task 2: Task: Build a stream that takes a Source of sentences, counts the words, and prints the total word count.
  val sentence = Source(List("Akka is Great, Streams are powerful, Scala is awesome"))
  sentence
    .flatMapConcat(s => Source(s.split(" ")))
    .fold(0)((count, _) => count + 1)
    .runWith(Sink.foreach(count => println(s"Total words: $count")))

  // Task 3: Read lines from a text file and print them to the console.
  val fileSource = FileIO.fromPath(Paths.get("factorials.txt"))
  fileSource
    .via(Framing.delimiter(ByteString("\n"), maximumFrameLength = 256, allowTruncation = true))
    .map(_.utf8String)
    .runWith(Sink.foreach(println))

  // Task 4: Create a Source from a collection of Tweet objects, filter them by a specific hashtag, and collect the results into a list.
}
