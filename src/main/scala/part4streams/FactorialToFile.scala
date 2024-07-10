package part4streams

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{FileIO, Source}
import akka.stream.{IOResult, Materializer}
import akka.util.ByteString

import java.nio.file.Paths
import scala.concurrent.Future

// Create a stream that writes the factorials of the first 100 numbers to a file
object FactorialToFile extends App {
  // Set up ActorSystem and Materialize

  // We use implicit for ActorSystem and Materializer to automatically pass them to Akka Streams' methods that require them for execution.
  implicit val system = ActorSystem("factorialToFile")
  implicit val materializer: Materializer = Materializer(system)
  // imports the implicit ExecutionContext needed for managing asynchronous operations and Future handling in Akka Streams.
  import system.dispatcher

  // How to start a stream of data.
  val source: Source[Int, NotUsed] = Source(1 to 5) //  Creates a Source that generates a stream of integers from 1 to 100.

  // How to perform a cumulative operation on stream data.
  val factorials = source.scan(BigInt(1))((acc, next) => acc * next) // Uses the scan operator to compute the
  // factorial of each number. The initial value is BigInt(1), and for each integer in the stream, it calculates the factorial (acc * next).

  // Convert to ByteString and Write to File
  val result: Future[IOResult] =
    factorials
      .map(num => ByteString(s"$num\n"))
      .runWith(FileIO.toPath(Paths.get("factorials.txt")))

  // Shut down the ActorSystem after the stream completes
  result.onComplete { _ =>
    system.terminate()
  }
}
