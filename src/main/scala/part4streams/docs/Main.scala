package part4streams.docs

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.IOResult
import akka.stream.scaladsl.{FileIO, Flow, Keep, Sink, Source}
import akka.util.ByteString

import java.nio.file.Paths
import scala.concurrent.Future

object Main extends App {
  implicit val system = ActorSystem("QuickStart")

  val source = Source(1 to 10)
  // This is a Source
//  source.runForeach(i => println(i)) // 1 2 3 4 5 6 7 8 9 10

  val factorials = source.scan(BigInt(1))((acc, next) => acc * next)

  // This is a Sink i.e the computed value is stored in a destination file (factorials.txt)
  val result: Future[IOResult] =
    factorials.map(num => ByteString(s"$num\n")).runWith(FileIO.toPath(Paths.get("src/main/scala/part4streams/docs/factorials.txt")))

  def lineSink(filename: String): Sink[String, Future[IOResult]] =
    Flow[String].map(s => ByteString(s + "\n")).toMat(FileIO.toPath(Paths.get(filename)))(Keep.right)

  factorials.map(_.toString).runWith(lineSink("src/main/scala/part4streams/docs/factorial2.txt"))
}
